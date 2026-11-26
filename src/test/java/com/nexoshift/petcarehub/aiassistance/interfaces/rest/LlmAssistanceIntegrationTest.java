package com.nexoshift.petcarehub.aiassistance.interfaces.rest;

import com.nexoshift.petcarehub.support.PetCareApiClient;
import com.nexoshift.petcarehub.support.PetCareApiClient.ClinicSetup;
import com.nexoshift.petcarehub.support.PetCareApiClient.OwnerSetup;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * LLM del veterinario (borrador SOAP) y chat del dueño con datos reales de IAM, Profiles y
 * Clinical Attention. El LLM es el falso (ai.llm.provider=fake): no gasta créditos de OpenAI.
 */
@SpringBootTest(properties = "spring.profiles.active=test")
class LlmAssistanceIntegrationTest {

  private static final String CONSULTATIONS = "/api/v1/clinical-consultations";
  private static final String ASSISTANCES = "/api/v1/ai/clinical-assistances";
  private static final String EXPLANATIONS = "/api/v1/ai/owner-explanations";

  @Autowired
  private WebApplicationContext context;

  private PetCareApiClient api;
  private ClinicSetup clinic;
  private OwnerSetup owner;

  @BeforeEach
  void setUp() throws Exception {
    api = new PetCareApiClient(MockMvcBuilders.webAppContextSetup(context)
        .apply(SecurityMockMvcConfigurers.springSecurity()).build());
    clinic = api.clinicWithVeterinarian();
    owner = api.ownerWithPet();
    api.registerPatient(clinic, owner.petId());
  }

  private String vet() {
    return clinic.holder().token();
  }

  private long startedConsultation(String reason) throws Exception {
    var created = api.send(HttpMethod.POST, CONSULTATIONS, vet(), """
        {"petId": %d, "clinicId": %d, "reason": "%s"}"""
        .formatted(owner.petId(), clinic.clinicId(), reason));
    assertThat(created.status()).as(created.body()).isEqualTo(201);
    var id = created.number("id");
    api.send(HttpMethod.PUT, CONSULTATIONS + "/" + id + "/start", vet(), null);
    return id;
  }

  private long completedConsultation() throws Exception {
    var id = startedConsultation("Itchy ears");
    var base = CONSULTATIONS + "/" + id;
    api.send(HttpMethod.POST, base + "/anamnesis", vet(), """
        {"chiefComplaint": "Shakes the head", "evolutionTime": "1 week",
         "symptoms": ["Itching"], "diet": "Dry food"}""");
    api.send(HttpMethod.POST, base + "/physiological-assessments", vet(), """
        {"weightKg": 10, "temperatureCelsius": 38.5, "heartRateBpm": 100,
         "respiratoryRateBpm": 25}""");
    api.send(HttpMethod.POST, base + "/diagnoses", vet(), """
        {"description": "Malassezia otitis", "type": "PRESUMPTIVE", "primary": true}""");
    assertThat(api.send(HttpMethod.PUT, base + "/complete", vet(), null).status()).isEqualTo(200);
    return id;
  }

  private String assistanceBody(long consultationId, Long backgroundId) {
    var backgrounds = backgroundId == null ? "[]"
        : "[{\"type\": \"PREVIOUS_CONSULTATION\", \"referenceId\": " + backgroundId + "}]";
    return """
        {"consultationId": %d, "petId": %d, "currentReason": "Itchy ears again",
         "backgrounds": %s}""".formatted(consultationId, owner.petId(), backgrounds);
  }

  @Test
  @DisplayName("Backgrounds list the completed consultations of the pet")
  void backgrounds() throws Exception {
    var previous = completedConsultation();

    var reply = api.send(HttpMethod.GET, "/api/v1/ai/clinical-backgrounds?petId=" + owner.petId(),
        vet(), null);

    assertThat(reply.status()).as(reply.body()).isEqualTo(200);
    assertThat(reply.body()).contains("\"type\":\"PREVIOUS_CONSULTATION\"",
        "\"referenceId\":" + previous, "Malassezia otitis");
  }

  @Test
  @DisplayName("A vet of another clinic cannot list the backgrounds")
  void backgroundsOtherClinic() throws Exception {
    var other = api.clinicWithVeterinarian();

    assertThat(api.send(HttpMethod.GET, "/api/v1/ai/clinical-backgrounds?petId=" + owner.petId(),
        other.holder().token(), null).status()).isEqualTo(403);
  }

  @Test
  @DisplayName("The vet gets a SOAP draft, accepts it and downloads the PDF")
  void soapDraftFlow() throws Exception {
    var previous = completedConsultation();
    var current = startedConsultation("Itchy ears again");

    var created = api.send(HttpMethod.POST, ASSISTANCES, vet(), assistanceBody(current, previous));
    assertThat(created.status()).as(created.body()).isEqualTo(201);
    assertThat(created.body()).contains("\"status\":\"COMPLETED\"", "\"decision\":\"PENDING\"");
    var id = created.number("id");
    var draftId = Long.parseLong(PetCareApiClient.extract(created.body(),
        "\"drafts\":\\[\\{\"id\":(\\d+)"));

    var accepted = api.send(HttpMethod.PUT,
        ASSISTANCES + "/" + id + "/drafts/" + draftId + "/decision", vet(),
        "{\"decision\": \"ACCEPTED\"}");
    assertThat(accepted.status()).as(accepted.body()).isEqualTo(200);
    assertThat(accepted.body()).contains("\"decision\":\"ACCEPTED\"", "\"reportUrl\":\"");

    var byConsultation = api.send(HttpMethod.GET, ASSISTANCES + "?consultationId=" + current,
        vet(), null);
    assertThat(byConsultation.status()).isEqualTo(200);
  }

  @Test
  @DisplayName("A draft needs the current consultation IN_PROGRESS and of the same pet")
  void draftNeedsCurrentConsultation() throws Exception {
    var completed = completedConsultation();
    var otherOwner = api.ownerWithPet();
    api.registerPatient(clinic, otherOwner.petId());

    var notInProgress = api.send(HttpMethod.POST, ASSISTANCES, vet(),
        assistanceBody(completed, null));
    var otherPet = api.send(HttpMethod.POST, ASSISTANCES, vet(), """
        {"consultationId": %d, "petId": %d, "currentReason": "x", "backgrounds": []}"""
        .formatted(startedConsultation("x"), otherOwner.petId()));

    assertThat(notInProgress.status()).isEqualTo(409);
    assertThat(otherPet.status()).isEqualTo(400);
  }

  @Test
  @DisplayName("A vet of another clinic cannot request a draft for the pet")
  void draftOtherClinic() throws Exception {
    var current = startedConsultation("x");
    var other = api.clinicWithVeterinarian();

    assertThat(api.send(HttpMethod.POST, ASSISTANCES, other.holder().token(),
        assistanceBody(current, null)).status()).isEqualTo(403);
  }

  @Test
  @DisplayName("A background of another pet is rejected")
  void backgroundOfAnotherPet() throws Exception {
    var otherOwner = api.ownerWithPet();
    api.registerPatient(clinic, otherOwner.petId());
    var otherPetConsultation = api.send(HttpMethod.POST, CONSULTATIONS, vet(), """
        {"petId": %d, "clinicId": %d, "reason": "x"}"""
        .formatted(otherOwner.petId(), clinic.clinicId())).number("id");
    var current = startedConsultation("x");

    assertThat(api.send(HttpMethod.POST, ASSISTANCES, vet(),
        assistanceBody(current, otherPetConsultation)).status()).isEqualTo(400);
  }

  @Test
  @DisplayName("The owner asks about the pet and sees the chat history")
  void ownerExplanation() throws Exception {
    completedConsultation();

    var asked = api.send(HttpMethod.POST, EXPLANATIONS, owner.owner().token(), """
        {"petId": %d, "question": "What is otitis?"}""".formatted(owner.petId()));
    var history = api.send(HttpMethod.GET, EXPLANATIONS + "?petId=" + owner.petId(),
        owner.owner().token(), null);

    assertThat(asked.status()).as(asked.body()).isEqualTo(201);
    assertThat(asked.text("answer")).isNotBlank();
    assertThat(history.body()).contains("What is otitis?");
  }

  @Test
  @DisplayName("Nobody else can ask about the pet: another owner or a vet get 403")
  void ownerExplanationAccess() throws Exception {
    var stranger = api.ownerWithPet();
    var body = "{\"petId\": %d, \"question\": \"Is it serious?\"}".formatted(owner.petId());

    assertThat(api.send(HttpMethod.POST, EXPLANATIONS, stranger.owner().token(), body).status())
        .isEqualTo(403);
    assertThat(api.send(HttpMethod.POST, EXPLANATIONS, vet(), body).status()).isEqualTo(403);
  }
}

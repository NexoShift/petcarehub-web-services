package com.nexoshift.petcarehub.clinicalattention.interfaces.rest;

import com.nexoshift.petcarehub.support.PetCareApiClient;
import com.nexoshift.petcarehub.support.PetCareApiClient.ClinicSetup;
import com.nexoshift.petcarehub.support.PetCareApiClient.OwnerSetup;
import com.nexoshift.petcarehub.support.PetCareApiClient.Reply;
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
 * End-to-end test of the REST API with the real security chain, JPA mappings and an H2 database
 * (profile "test").
 * <p>
 * CAMBIO: antes usaba ids inventados (petId 5, clinicId 3) porque el adaptador "lenient" decía
 * sí a todo. Ahora cada test crea datos reales por la API de IAM y Profiles:
 * <pre>
 *   clinic : cuenta CLINIC + clínica + perfil vet del titular + consultorio activo
 *   owner  : cuenta PET_OWNER + perfil dueño + mascota, registrada como paciente de "clinic"
 * </pre>
 * Así se prueba la comunicación real Clinical Attention -> Profiles.
 */
@SpringBootTest(properties = "spring.profiles.active=test")
class ClinicalConsultationsControllerIntegrationTest {

  private static final String BASE = "/api/v1/clinical-consultations";

  private static final String ANAMNESIS_BODY = """
      {"chiefComplaint": "Vomiting", "evolutionTime": "2 days",
       "symptoms": ["Vomiting", "Lethargy"], "diet": "Dry food"}""";
  private static final String ASSESSMENT_BODY = """
      {"weightKg": 12.5, "temperatureCelsius": 38.6, "heartRateBpm": 110,
       "respiratoryRateBpm": 28, "bodyConditionScore": 5, "hydrationStatus": "MILD_DEHYDRATION"}""";
  private static final String FINDING_BODY = """
      {"bodySystem": "DIGESTIVE", "description": "Painful abdomen", "severity": "MODERATE"}""";
  private static final String PRIMARY_DIAGNOSIS_BODY = """
      {"description": "Acute gastroenteritis", "type": "PRESUMPTIVE", "primary": true,
       "differentialDiagnoses": ["Pancreatitis"], "rationale": "Vomiting after a food change"}""";
  private static final String REEVALUATION_BODY = """
      {"description": "Pancreatitis", "type": "DEFINITIVE", "differentialDiagnoses": [],
       "rationale": "Lipase elevated"}""";

  @Autowired
  private WebApplicationContext context;

  private PetCareApiClient api;
  private ClinicSetup clinic;
  private OwnerSetup owner;
  private String veterinarianToken;

  @BeforeEach
  void setUp() throws Exception {
    var mockMvc = MockMvcBuilders.webAppContextSetup(context)
        .apply(SecurityMockMvcConfigurers.springSecurity())
        .build();
    api = new PetCareApiClient(mockMvc);
    clinic = api.clinicWithVeterinarian();
    owner = api.ownerWithPet();
    api.registerPatient(clinic, owner.petId());
    veterinarianToken = clinic.holder().token();
  }

  // ------------------------------------------------------------------ helpers

  private Reply send(HttpMethod method, String url, String token, String json) throws Exception {
    return api.send(method, url, token, json);
  }

  private String createBody(long petId, long clinicId, Long roomId) {
    return """
        {"petId": %d, "clinicId": %d, "consultingRoomId": %s, "reason": "Vomits since yesterday"}"""
        .formatted(petId, clinicId, roomId == null ? "null" : roomId.toString());
  }

  private String validBody() {
    return createBody(owner.petId(), clinic.clinicId(), clinic.consultingRoomId());
  }

  private long createConsultation(String token) throws Exception {
    var reply = send(HttpMethod.POST, BASE, token, validBody());
    assertThat(reply.status()).as(reply.body()).isEqualTo(201);
    return reply.number("id");
  }

  private long startedConsultation() throws Exception {
    var id = createConsultation(veterinarianToken);
    assertThat(send(HttpMethod.PUT, BASE + "/" + id + "/start", veterinarianToken, null).status())
        .isEqualTo(200);
    return id;
  }

  // ----------------------------------------------------------------- security

  @Test
  @DisplayName("Without a token the API answers 401")
  void requiresAuthentication() throws Exception {
    assertThat(send(HttpMethod.POST, BASE, null, validBody()).status()).isEqualTo(401);
    assertThat(send(HttpMethod.GET, BASE + "/1", null, null).status()).isEqualTo(401);
  }

  @Test
  @DisplayName("A pet owner cannot create or change consultations")
  void petOwnerCannotWrite() throws Exception {
    var id = createConsultation(veterinarianToken);
    var ownerToken = owner.owner().token();

    assertThat(send(HttpMethod.POST, BASE, ownerToken, validBody()).status()).isEqualTo(403);
    assertThat(send(HttpMethod.PUT, BASE + "/" + id + "/start", ownerToken, null).status())
        .isEqualTo(403);
  }

  @Test
  @DisplayName("NUEVO: the owner sees the detail of a consultation of their pet; another owner does not")
  void ownerReadsOwnConsultation() throws Exception {
    var id = createConsultation(veterinarianToken);
    var otherOwner = api.ownerWithPet();

    assertThat(send(HttpMethod.GET, BASE + "/" + id, owner.owner().token(), null).status())
        .isEqualTo(200);
    assertThat(send(HttpMethod.GET, BASE + "/" + id, otherOwner.owner().token(), null).status())
        .isEqualTo(403);
  }

  @Test
  @DisplayName("Only the veterinarian in charge can change a consultation; the clinic colleague can read it")
  void otherVeterinarianOfSameClinic() throws Exception {
    var id = startedConsultation();
    var colleague = api.addVeterinarian(clinic).member().token();

    assertThat(send(HttpMethod.PUT, BASE + "/" + id + "/cancel", colleague,
        "{\"reason\": \"Not mine\"}").status()).isEqualTo(403);
    assertThat(send(HttpMethod.POST, BASE + "/" + id + "/anamnesis", colleague,
        ANAMNESIS_BODY).status()).isEqualTo(403);
    assertThat(send(HttpMethod.GET, BASE + "/" + id, colleague, null).status()).isEqualTo(200);
  }

  @Test
  @DisplayName("NUEVO: a veterinarian of another clinic cannot read the consultation (403)")
  void veterinarianOfOtherClinicCannotRead() throws Exception {
    var id = createConsultation(veterinarianToken);
    var otherClinic = api.clinicWithVeterinarian();

    assertThat(send(HttpMethod.GET, BASE + "/" + id, otherClinic.holder().token(), null).status())
        .isEqualTo(403);
  }

  // ------------------------------------------------------------------- create

  @Test
  @DisplayName("POST creates a CREATED consultation and returns its location")
  void createsConsultation() throws Exception {
    var reply = send(HttpMethod.POST, BASE, veterinarianToken, validBody());

    assertThat(reply.status()).isEqualTo(201);
    var id = reply.number("id");
    assertThat(reply.location()).endsWith(BASE + "/" + id);
    assertThat(reply.body()).contains("\"status\":\"CREATED\"");
    assertThat(reply.body()).contains("\"petId\":" + owner.petId());
    assertThat(reply.body()).contains("\"reason\":\"Vomits since yesterday\"");
  }

  @Test
  @DisplayName("NUEVO: veterinarianId is the id of the veterinarian profile, not the IAM user id")
  void storesVeterinarianProfileId() throws Exception {
    var reply = send(HttpMethod.POST, BASE, veterinarianToken, validBody());

    assertThat(reply.number("veterinarianId")).isEqualTo(clinic.veterinarianId());
  }

  @Test
  @DisplayName("NUEVO: the pet, clinic, patient and consulting room are validated against Profiles")
  void validatesAgainstProfiles() throws Exception {
    var otherClinic = api.clinicWithVeterinarian();
    var notPatient = api.ownerWithPet();

    var unknownPet = send(HttpMethod.POST, BASE, veterinarianToken,
        createBody(999_999L, clinic.clinicId(), null));
    var foreignClinic = send(HttpMethod.POST, BASE, veterinarianToken,
        createBody(owner.petId(), otherClinic.clinicId(), null));
    var petNotPatient = send(HttpMethod.POST, BASE, veterinarianToken,
        createBody(notPatient.petId(), clinic.clinicId(), null));
    var unknownRoom = send(HttpMethod.POST, BASE, veterinarianToken,
        createBody(owner.petId(), clinic.clinicId(), 999_999L));

    assertThat(unknownPet.status()).isEqualTo(404);
    assertThat(foreignClinic.status()).isEqualTo(403);
    assertThat(petNotPatient.status()).isEqualTo(422);
    assertThat(unknownRoom.status()).isEqualTo(400);
  }

  @Test
  @DisplayName("Invalid data is answered with 400 and the invalid fields")
  void rejectsInvalidData() throws Exception {
    var blankReason = send(HttpMethod.POST, BASE, veterinarianToken,
        "{\"petId\": 5, \"clinicId\": 3, \"reason\": \" \"}");
    var missingPet = send(HttpMethod.POST, BASE, veterinarianToken,
        "{\"clinicId\": 3, \"reason\": \"Check-up\"}");
    var malformed = send(HttpMethod.POST, BASE, veterinarianToken, "{\"petId\": ");

    assertThat(blankReason.status()).isEqualTo(400);
    assertThat(blankReason.body()).contains("reason");
    assertThat(missingPet.status()).isEqualTo(400);
    assertThat(missingPet.body()).contains("petId");
    assertThat(malformed.status()).isEqualTo(400);
  }

  @Test
  @DisplayName("GET returns the consultation and 404 when it does not exist")
  void getsConsultation() throws Exception {
    var id = createConsultation(veterinarianToken);

    var found = send(HttpMethod.GET, BASE + "/" + id, veterinarianToken, null);
    var missing = send(HttpMethod.GET, BASE + "/999999", veterinarianToken, null);

    assertThat(found.status()).isEqualTo(200);
    assertThat(found.body()).contains("\"id\":" + id);
    assertThat(missing.status()).isEqualTo(404);
  }

  // -------------------------------------------------------------------- flow

  @Test
  @DisplayName("Records need the consultation to be started")
  void recordsNeedStartedConsultation() throws Exception {
    var id = createConsultation(veterinarianToken);

    var reply = send(HttpMethod.POST, BASE + "/" + id + "/anamnesis", veterinarianToken,
        ANAMNESIS_BODY);

    assertThat(reply.status()).isEqualTo(409);
  }

  @Test
  @DisplayName("Complete flow: records, reevaluation without overwriting, completion and closing")
  void completeFlow() throws Exception {
    var id = startedConsultation();
    var url = BASE + "/" + id;

    var anamnesis = send(HttpMethod.POST, url + "/anamnesis", veterinarianToken, ANAMNESIS_BODY);
    assertThat(anamnesis.status()).isEqualTo(201);
    assertThat(anamnesis.body()).contains("\"chiefComplaint\":\"Vomiting\"");
    assertThat(anamnesis.body()).contains("\"symptoms\":[\"Vomiting\",\"Lethargy\"]");

    assertThat(send(HttpMethod.POST, url + "/anamnesis", veterinarianToken, ANAMNESIS_BODY)
        .status()).isEqualTo(409);

    assertThat(send(HttpMethod.POST, url + "/physiological-assessments", veterinarianToken,
        ASSESSMENT_BODY).status()).isEqualTo(201);
    var secondAssessment = send(HttpMethod.POST, url + "/physiological-assessments",
        veterinarianToken, ASSESSMENT_BODY);
    assertThat(secondAssessment.status()).isEqualTo(201);
    assertThat(secondAssessment.body().split("\"weightKg\"", -1).length - 1).isEqualTo(2);

    var finding = send(HttpMethod.POST, url + "/findings", veterinarianToken, FINDING_BODY);
    assertThat(finding.status()).isEqualTo(201);
    assertThat(finding.body()).contains("\"bodySystem\":\"DIGESTIVE\"");

    var diagnosis = send(HttpMethod.POST, url + "/diagnoses", veterinarianToken,
        PRIMARY_DIAGNOSIS_BODY);
    assertThat(diagnosis.status()).isEqualTo(201);
    var diagnosisId = PetCareApiClient.extract(diagnosis.body(),
        "\"diagnoses\":\\[\\{\"id\":(\\d+)");

    // a second primary diagnosis is not allowed: it must be a reevaluation
    assertThat(send(HttpMethod.POST, url + "/diagnoses", veterinarianToken, PRIMARY_DIAGNOSIS_BODY)
        .status()).isEqualTo(409);

    var reevaluation = send(HttpMethod.POST, url + "/diagnoses/" + diagnosisId + "/reevaluations",
        veterinarianToken, REEVALUATION_BODY);
    assertThat(reevaluation.status()).isEqualTo(201);
    assertThat(reevaluation.body()).contains("\"status\":\"SUPERSEDED\"");
    assertThat(reevaluation.body()).contains("\"description\":\"Acute gastroenteritis\"");
    assertThat(reevaluation.body()).contains("\"description\":\"Pancreatitis\"");
    assertThat(reevaluation.body()).contains("\"previousDiagnosisId\":" + diagnosisId);

    var completed = send(HttpMethod.PUT, url + "/complete", veterinarianToken, null);
    assertThat(completed.status()).isEqualTo(200);
    assertThat(completed.body()).contains("\"status\":\"COMPLETED\"");

    // the consultation is closed
    assertThat(send(HttpMethod.POST, url + "/findings", veterinarianToken, FINDING_BODY).status())
        .isEqualTo(409);
    assertThat(send(HttpMethod.PUT, url + "/cancel", veterinarianToken, "{\"reason\": \"Mistake\"}")
        .status()).isEqualTo(409);

    var stored = send(HttpMethod.GET, url, veterinarianToken, null);
    assertThat(stored.body()).contains("\"status\":\"COMPLETED\"");
    assertThat(stored.body()).contains("\"previousDiagnosisId\":" + diagnosisId);
  }

  @Test
  @DisplayName("Completing without the required data answers 409 with the pending items")
  void incompleteCompletion() throws Exception {
    var id = startedConsultation();

    var reply = send(HttpMethod.PUT, BASE + "/" + id + "/complete", veterinarianToken, null);

    assertThat(reply.status()).isEqualTo(409);
    assertThat(reply.body()).contains("anamnesis");
    assertThat(reply.body()).contains("physiologicalAssessment");
    assertThat(reply.body()).contains("primaryDiagnosis");
    var stored = send(HttpMethod.GET, BASE + "/" + id, veterinarianToken, null);
    assertThat(stored.body()).contains("\"status\":\"IN_PROGRESS\"");
  }

  @Test
  @DisplayName("Invalid clinical data is answered with 400")
  void rejectsInvalidClinicalData() throws Exception {
    var id = startedConsultation();
    var url = BASE + "/" + id;

    var coldPet = send(HttpMethod.POST, url + "/physiological-assessments", veterinarianToken,
        ASSESSMENT_BODY.replace("38.6", "20"));
    var unknownBodySystem = send(HttpMethod.POST, url + "/findings", veterinarianToken,
        FINDING_BODY.replace("DIGESTIVE", "TAIL"));
    var withoutType = send(HttpMethod.POST, url + "/diagnoses", veterinarianToken,
        "{\"description\": \"Gastritis\", \"primary\": true}");
    var unknownDiagnosis = send(HttpMethod.POST, url + "/diagnoses/999999/reevaluations",
        veterinarianToken, REEVALUATION_BODY);

    assertThat(coldPet.status()).isEqualTo(400);
    assertThat(unknownBodySystem.status()).isEqualTo(400);
    assertThat(withoutType.status()).isEqualTo(400);
    assertThat(unknownDiagnosis.status()).isEqualTo(400);
  }

  @Test
  @DisplayName("Cancelling needs a reason and keeps the consultation as CANCELLED")
  void cancelsConsultation() throws Exception {
    var id = createConsultation(veterinarianToken);
    var url = BASE + "/" + id + "/cancel";

    assertThat(send(HttpMethod.PUT, url, veterinarianToken, "{\"reason\": \"\"}").status())
        .isEqualTo(400);
    var cancelled = send(HttpMethod.PUT, url, veterinarianToken, "{\"reason\": \"No show\"}");

    assertThat(cancelled.status()).isEqualTo(200);
    assertThat(cancelled.body()).contains("\"status\":\"CANCELLED\"");
    assertThat(cancelled.body()).contains("\"cancellationReason\":\"No show\"");
    assertThat(send(HttpMethod.PUT, url, veterinarianToken, "{\"reason\": \"Again\"}").status())
        .isEqualTo(409);
  }

  // --------------------------------------------------------------- by pet

  @Test
  @DisplayName("A veterinarian of the clinic lists the consultations of a patient, newest first")
  void listsConsultationsOfPet() throws Exception {
    var first = createConsultation(veterinarianToken);
    var second = createConsultation(veterinarianToken);

    var reply = send(HttpMethod.GET, "/api/v1/pets/" + owner.petId() + "/clinical-consultations",
        veterinarianToken, null);

    assertThat(reply.status()).isEqualTo(200);
    var secondPosition = reply.body().indexOf("\"id\":" + second + ",");
    var firstPosition = reply.body().indexOf("\"id\":" + first + ",");
    assertThat(secondPosition >= 0).isTrue();
    assertThat(secondPosition < firstPosition).isTrue();
  }

  @Test
  @DisplayName("NUEVO: the history of a pet: owner yes, another owner no, vet of another clinic no")
  void petHistoryAccess() throws Exception {
    createConsultation(veterinarianToken);
    var url = "/api/v1/pets/" + owner.petId() + "/clinical-consultations";
    var otherOwner = api.ownerWithPet();
    var otherClinic = api.clinicWithVeterinarian();

    assertThat(send(HttpMethod.GET, url, owner.owner().token(), null).status()).isEqualTo(200);
    assertThat(send(HttpMethod.GET, url, otherOwner.owner().token(), null).status())
        .isEqualTo(403);
    assertThat(send(HttpMethod.GET, url, otherClinic.holder().token(), null).status())
        .isEqualTo(403);

    // cuando la otra clínica registra a la mascota como paciente, ya puede ver los antecedentes
    api.registerPatient(otherClinic, owner.petId());
    assertThat(send(HttpMethod.GET, url, otherClinic.holder().token(), null).status())
        .isEqualTo(200);
  }

  @Test
  @DisplayName("The consultations of a pet need authentication")
  void petConsultationsNeedAuthentication() throws Exception {
    assertThat(send(HttpMethod.GET, "/api/v1/pets/5/clinical-consultations", null, null).status())
        .isEqualTo(401);
  }
}

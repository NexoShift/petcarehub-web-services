package com.nexoshift.petcarehub.medicalhistory.interfaces.rest;

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

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Medical History con IAM, Profiles, Clinical Attention y Clinical Exams reales (perfil test).
 * La línea de tiempo se llena sola con los eventos de consulta completada y resultado validado.
 */
@SpringBootTest(properties = "spring.profiles.active=test")
class MedicalHistoryIntegrationTest {

  private static final String CONSULTATIONS = "/api/v1/clinical-consultations";
  private static final String EXAMS = "/api/v1/clinical-exams";
  private static final String ALLERGY = """
      {"allergen": "Penicillin", "reaction": "Hives", "severity": "SEVERE"}""";

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

  private String history() {
    return "/api/v1/pets/" + owner.petId() + "/medical-history";
  }

  private String vet() {
    return clinic.holder().token();
  }

  private String vaccination(LocalDate appliedOn, LocalDate nextDoseOn) {
    return """
        {"vaccineName": "Rabies", "doseNumber": 1, "appliedOn": "%s", "nextDoseOn": %s,
         "batchNumber": "RB-2026-01"}"""
        .formatted(appliedOn, nextDoseOn == null ? "null" : "\"" + nextDoseOn + "\"");
  }

  private long startedConsultation() throws Exception {
    var created = api.send(HttpMethod.POST, CONSULTATIONS, vet(), """
        {"petId": %d, "clinicId": %d, "reason": "Vomits"}"""
        .formatted(owner.petId(), clinic.clinicId()));
    assertThat(created.status()).as(created.body()).isEqualTo(201);
    var id = created.number("id");
    assertThat(api.send(HttpMethod.PUT, CONSULTATIONS + "/" + id + "/start", vet(), null)
        .status()).isEqualTo(200);
    return id;
  }

  private void completeConsultation(long id) throws Exception {
    var base = CONSULTATIONS + "/" + id;
    var anamnesis = api.send(HttpMethod.POST, base + "/anamnesis", vet(),
        "{\"chiefComplaint\": \"Vomiting\", \"evolutionTime\": \"2 days\","
            + " \"symptoms\": [\"Vomiting\"], \"diet\": \"Dry food\"}");
    assertThat(anamnesis.status()).as(anamnesis.body()).isEqualTo(201);
    api.send(HttpMethod.POST, base + "/physiological-assessments", vet(),
        "{\"weightKg\": 10, \"temperatureCelsius\": 38.5, \"heartRateBpm\": 100,"
            + " \"respiratoryRateBpm\": 25}");
    api.send(HttpMethod.POST, base + "/diagnoses", vet(),
        "{\"description\": \"Acute gastroenteritis\", \"type\": \"PRESUMPTIVE\", \"primary\": true}");
    var completed = api.send(HttpMethod.PUT, base + "/complete", vet(), null);
    assertThat(completed.status()).as(completed.body()).isEqualTo(200);
  }

  @Test
  @DisplayName("A pet without records has an empty history")
  void emptyHistory() throws Exception {
    var reply = api.send(HttpMethod.GET, history(), owner.owner().token(), null);

    assertThat(reply.status()).as(reply.body()).isEqualTo(200);
    assertThat(reply.body()).contains("\"petId\":" + owner.petId(), "\"allergies\":[]",
        "\"vaccinations\":[]", "\"entries\":[]");
  }

  @Test
  @DisplayName("The owner reports an allergy and the vet sees it")
  void ownerReportsAllergy() throws Exception {
    var created = api.send(HttpMethod.POST, history() + "/allergies", owner.owner().token(),
        ALLERGY);
    var seenByVet = api.send(HttpMethod.GET, history(), vet(), null);

    assertThat(created.status()).as(created.body()).isEqualTo(201);
    assertThat(created.body()).contains("\"reportedBy\":\"OWNER\"");
    assertThat(seenByVet.body()).contains("Penicillin", "\"severity\":\"SEVERE\"");
  }

  @Test
  @DisplayName("The treating vet records a vaccination; the owner cannot")
  void vaccination() throws Exception {
    var today = LocalDate.now();

    var byVet = api.send(HttpMethod.POST, history() + "/vaccinations", vet(),
        vaccination(today, today.plusYears(1)));
    var byOwner = api.send(HttpMethod.POST, history() + "/vaccinations", owner.owner().token(),
        vaccination(today, null));

    assertThat(byVet.status()).as(byVet.body()).isEqualTo(201);
    assertThat(byVet.body()).contains("\"vaccineName\":\"Rabies\"",
        "\"clinicId\":" + clinic.clinicId(), "\"veterinarianId\":" + clinic.veterinarianId());
    assertThat(byOwner.status()).isEqualTo(403);
  }

  @Test
  @DisplayName("Vaccination dates: not in the future, booster after the application")
  void vaccinationDates() throws Exception {
    var today = LocalDate.now();

    assertThat(api.send(HttpMethod.POST, history() + "/vaccinations", vet(),
        vaccination(today.plusDays(1), null)).status()).isEqualTo(400);
    assertThat(api.send(HttpMethod.POST, history() + "/vaccinations", vet(),
        vaccination(today, today.minusDays(1))).status()).isEqualTo(400);
  }

  @Test
  @DisplayName("Strangers and vets of clinics where the pet is not a patient get 403")
  void strangers() throws Exception {
    var stranger = api.ownerWithPet();
    var otherClinic = api.clinicWithVeterinarian();

    assertThat(api.send(HttpMethod.GET, history(), stranger.owner().token(), null).status())
        .isEqualTo(403);
    assertThat(api.send(HttpMethod.GET, history(), otherClinic.holder().token(), null).status())
        .isEqualTo(403);
    assertThat(api.send(HttpMethod.POST, history() + "/allergies",
        otherClinic.holder().token(), ALLERGY).status()).isEqualTo(403);
  }

  @Test
  @DisplayName("An unknown pet answers 404")
  void unknownPet() throws Exception {
    assertThat(api.send(HttpMethod.GET, "/api/v1/pets/999999/medical-history", vet(), null)
        .status()).isEqualTo(404);
  }

  @Test
  @DisplayName("A completed consultation appears in the timeline with its primary diagnosis")
  void completedConsultationInTimeline() throws Exception {
    var consultationId = startedConsultation();
    completeConsultation(consultationId);

    var reply = api.send(HttpMethod.GET, history(), owner.owner().token(), null);

    assertThat(reply.body()).contains("\"type\":\"CONSULTATION_COMPLETED\"",
        "\"referenceId\":" + consultationId, "Acute gastroenteritis",
        "\"clinicId\":" + clinic.clinicId());
  }

  @Test
  @DisplayName("A validated exam result appears in the timeline")
  void validatedExamInTimeline() throws Exception {
    var consultationId = startedConsultation();
    var exam = api.send(HttpMethod.POST, EXAMS, vet(),
        "{\"consultationId\": %d, \"type\": \"LABORATORY\", \"indication\": \"Blood count\"}"
            .formatted(consultationId));
    assertThat(exam.status()).as(exam.body()).isEqualTo(201);
    var examId = exam.number("id");
    api.send(HttpMethod.PUT, EXAMS + "/" + examId + "/start", vet(), null);
    api.send(HttpMethod.POST, EXAMS + "/" + examId + "/result", vet(),
        "{\"summary\": \"Mild anemia\", \"values\": \"HCT 30%%\", \"interpretation\": \"Iron\","
            .formatted() + " \"resultDate\": \"" + LocalDate.now() + "\"}");
    var validated = api.send(HttpMethod.PUT, EXAMS + "/" + examId + "/result/validate", vet(),
        null);
    assertThat(validated.status()).as(validated.body()).isEqualTo(200);

    var reply = api.send(HttpMethod.GET, history(), owner.owner().token(), null);

    assertThat(reply.body()).contains("\"type\":\"EXAM_RESULT_VALIDATED\"",
        "\"referenceId\":" + examId, "\"examType\":\"LABORATORY\"", "Mild anemia");
  }
}

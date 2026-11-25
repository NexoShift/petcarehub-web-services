package com.nexoshift.petcarehub.clinicalattention.interfaces.rest;

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
 * Tratamientos y recetas de una consulta, con IAM y Profiles reales (perfil test, H2).
 */
@SpringBootTest(properties = "spring.profiles.active=test")
class TreatmentsAndPrescriptionsIntegrationTest {

  private static final String BASE = "/api/v1/clinical-consultations";

  private static final String PRESCRIPTION_BODY = """
      {"generalInstruction": "Return if vomiting continues",
       "medications": [
         {"name": "Metronidazole 250 mg", "dosage": "1 tablet", "frequency": "Every 12 hours",
          "duration": "7 days", "indication": "Give with food"},
         {"name": "Omeprazole 10 mg", "dosage": "1 tablet", "frequency": "Every 24 hours",
          "duration": "5 days"}]}""";

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

  private long consultation(boolean start) throws Exception {
    var created = api.send(HttpMethod.POST, BASE, vet(), """
        {"petId": %d, "clinicId": %d, "consultingRoomId": %d, "reason": "Vomits"}"""
        .formatted(owner.petId(), clinic.clinicId(), clinic.consultingRoomId()));
    assertThat(created.status()).as(created.body()).isEqualTo(201);
    var id = created.number("id");
    if (start) {
      assertThat(api.send(HttpMethod.PUT, BASE + "/" + id + "/start", vet(), null).status())
          .isEqualTo(200);
    }
    return id;
  }

  private String treatmentBody(LocalDate start, LocalDate end) {
    return """
        {"description": "Bland diet", "instruction": "Boiled chicken and rice, 3 times a day",
         "startDate": "%s", "endDate": %s}"""
        .formatted(start, end == null ? "null" : "\"" + end + "\"");
  }

  @Test
  @DisplayName("The vet registers a treatment and it comes back ACTIVE in the consultation")
  void registerTreatment() throws Exception {
    var id = consultation(true);
    var today = LocalDate.now();

    var reply = api.send(HttpMethod.POST, BASE + "/" + id + "/treatments", vet(),
        treatmentBody(today, today.plusDays(7)));

    assertThat(reply.status()).as(reply.body()).isEqualTo(201);
    assertThat(reply.body()).contains("\"treatments\":[{", "Bland diet", "\"status\":\"ACTIVE\"",
        "\"prescribedByVeterinarianId\":" + clinic.veterinarianId());
  }

  @Test
  @DisplayName("A treatment without end date is allowed")
  void treatmentWithoutEnd() throws Exception {
    var id = consultation(true);

    assertThat(api.send(HttpMethod.POST, BASE + "/" + id + "/treatments", vet(),
        treatmentBody(LocalDate.now(), null)).status()).isEqualTo(201);
  }

  @Test
  @DisplayName("A treatment that ends before it starts is rejected")
  void treatmentEndsBeforeStart() throws Exception {
    var id = consultation(true);
    var today = LocalDate.now();

    assertThat(api.send(HttpMethod.POST, BASE + "/" + id + "/treatments", vet(),
        treatmentBody(today, today.minusDays(1))).status()).isEqualTo(400);
  }

  @Test
  @DisplayName("The vet issues a prescription with two medications")
  void issuePrescription() throws Exception {
    var id = consultation(true);

    var reply = api.send(HttpMethod.POST, BASE + "/" + id + "/prescriptions", vet(),
        PRESCRIPTION_BODY);

    assertThat(reply.status()).as(reply.body()).isEqualTo(201);
    assertThat(reply.body()).contains("\"prescriptions\":[{", "Metronidazole 250 mg",
        "Omeprazole 10 mg", "Give with food", "\"issuedAt\":\"");
  }

  @Test
  @DisplayName("A prescription without medications is rejected with 400, not 500")
  void prescriptionWithoutMedications() throws Exception {
    var id = consultation(true);

    assertThat(api.send(HttpMethod.POST, BASE + "/" + id + "/prescriptions", vet(),
        "{\"generalInstruction\": \"x\"}").status()).isEqualTo(400);
    assertThat(api.send(HttpMethod.POST, BASE + "/" + id + "/prescriptions", vet(),
        "{\"medications\": []}").status()).isEqualTo(400);
    assertThat(api.send(HttpMethod.POST, BASE + "/" + id + "/prescriptions", vet(),
        "{\"medications\": [{\"name\": \"X\"}]}").status()).isEqualTo(400);
  }

  @Test
  @DisplayName("Treatments and prescriptions need the consultation IN_PROGRESS")
  void needsInProgress() throws Exception {
    var id = consultation(false);

    assertThat(api.send(HttpMethod.POST, BASE + "/" + id + "/treatments", vet(),
        treatmentBody(LocalDate.now(), null)).status()).isEqualTo(409);
    assertThat(api.send(HttpMethod.POST, BASE + "/" + id + "/prescriptions", vet(),
        PRESCRIPTION_BODY).status()).isEqualTo(409);
  }

  @Test
  @DisplayName("Only the veterinarian in charge can prescribe; the owner cannot")
  void onlyVetInCharge() throws Exception {
    var id = consultation(true);
    var otherClinic = api.clinicWithVeterinarian();

    assertThat(api.send(HttpMethod.POST, BASE + "/" + id + "/prescriptions",
        otherClinic.holder().token(), PRESCRIPTION_BODY).status()).isEqualTo(403);
    assertThat(api.send(HttpMethod.POST, BASE + "/" + id + "/prescriptions",
        owner.owner().token(), PRESCRIPTION_BODY).status()).isEqualTo(403);
  }

  @Test
  @DisplayName("The owner sees the prescription in the pet history")
  void ownerSeesPrescription() throws Exception {
    var id = consultation(true);
    api.send(HttpMethod.POST, BASE + "/" + id + "/prescriptions", vet(), PRESCRIPTION_BODY);

    var history = api.send(HttpMethod.GET,
        "/api/v1/pets/" + owner.petId() + "/clinical-consultations", owner.owner().token(), null);

    assertThat(history.status()).as(history.body()).isEqualTo(200);
    assertThat(history.body()).contains("Metronidazole 250 mg");
  }
}

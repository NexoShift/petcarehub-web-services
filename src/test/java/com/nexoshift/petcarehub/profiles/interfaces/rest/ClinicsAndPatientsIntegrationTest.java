package com.nexoshift.petcarehub.profiles.interfaces.rest;

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

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * API de Profiles para clínicas, veterinarios y pacientes.
 * Escenario: "clinic" y "otherClinic" (cada una con su vet titular) y "owner" con su mascota.
 */
@SpringBootTest(properties = "spring.profiles.active=test")
class ClinicsAndPatientsIntegrationTest {

  @Autowired
  private WebApplicationContext context;

  private PetCareApiClient api;
  private ClinicSetup clinic;
  private ClinicSetup otherClinic;
  private OwnerSetup owner;

  @BeforeEach
  void setUp() throws Exception {
    api = new PetCareApiClient(MockMvcBuilders.webAppContextSetup(context)
        .apply(SecurityMockMvcConfigurers.springSecurity()).build());
    clinic = api.clinicWithVeterinarian();
    otherClinic = api.clinicWithVeterinarian();
    owner = api.ownerWithPet();
  }

  private String token(ClinicSetup who) {
    return who.holder().token();
  }

  // ----------------------------------------------------------------- clinics

  @Test
  @DisplayName("A pet owner cannot create a clinic profile")
  void ownerCannotCreateClinic() throws Exception {
    var reply = api.sendForm("/api/v1/clinics", owner.owner().token(), Map.of(
        "accountId", owner.owner().accountId(), "name", "Falsa", "ruc", "20111111111"));

    assertThat(reply.status()).isEqualTo(403);
  }

  @Test
  @DisplayName("An account can only have one clinic profile, and the RUC must have 11 digits")
  void clinicRules() throws Exception {
    var again = api.sendForm("/api/v1/clinics", token(clinic), Map.of(
        "accountId", clinic.holder().accountId(), "name", "Otra", "ruc", "20222222222"));
    var newcomer = api.signUpAndSignIn("CLINIC");
    var badRuc = api.sendForm("/api/v1/clinics", newcomer.token(), Map.of(
        "accountId", newcomer.accountId(), "name", "Nueva", "ruc", "123"));

    assertThat(again.status()).isEqualTo(409);
    assertThat(badRuc.status()).isEqualTo(400);
  }

  @Test
  @DisplayName("A clinic account cannot create the profile of another account")
  void clinicForAnotherAccount() throws Exception {
    var newcomer = api.signUpAndSignIn("CLINIC");

    var reply = api.sendForm("/api/v1/clinics", token(clinic), Map.of(
        "accountId", newcomer.accountId(), "name", "Ajena", "ruc", "20333333333"));

    assertThat(reply.status()).isEqualTo(403);
  }

  @Test
  @DisplayName("The clinic is found by id and by account id")
  void readsClinic() throws Exception {
    var byId = api.send(HttpMethod.GET, "/api/v1/clinics/" + clinic.clinicId(),
        token(clinic), null);
    var byAccount = api.send(HttpMethod.GET,
        "/api/v1/clinics?accountId=" + clinic.holder().accountId(), token(clinic), null);

    assertThat(byId.status()).isEqualTo(200);
    assertThat(byAccount.status()).isEqualTo(200);
    assertThat(byAccount.number("id")).isEqualTo(clinic.clinicId());
  }

  @Test
  @DisplayName("Only the clinic admin updates their clinic")
  void updatesClinic() throws Exception {
    var url = "/api/v1/clinics/" + clinic.clinicId();
    var body = """
        {"name": "Veterinaria Renovada", "ruc": "20123456789", "address": "Av. 2",
         "phoneNumber": "987654321", "description": "24 horas"}""";

    var mine = api.send(HttpMethod.PUT, url, token(clinic), body);
    var notMine = api.send(HttpMethod.PUT, url, token(otherClinic), body);

    assertThat(mine.status()).isEqualTo(200);
    assertThat(mine.body()).contains("\"name\":\"Veterinaria Renovada\"");
    assertThat(notMine.status()).isEqualTo(403);
  }

  // ----------------------------------------------------------- veterinarians

  @Test
  @DisplayName("A clinic cannot register veterinarians in another clinic")
  void vetForAnotherClinic() throws Exception {
    var newcomer = api.signUpAndSignIn("CLINIC");

    var reply = api.sendForm("/api/v1/veterinarians", token(otherClinic), Map.of(
        "userId", newcomer.userId(), "clinicId", clinic.clinicId(),
        "firstName", "X", "lastName", "Y", "email", "x@petcare.test"));

    assertThat(reply.status()).isEqualTo(403);
  }

  @Test
  @DisplayName("The clinic lists its veterinarians, including members; without a token it is 401")
  void listsVeterinarians() throws Exception {
    var member = api.addVeterinarian(clinic);
    var url = "/api/v1/clinics/" + clinic.clinicId() + "/veterinarians";

    var mine = api.send(HttpMethod.GET, url, token(clinic), null);
    assertThat(mine.status()).isEqualTo(200);
    assertThat(mine.body()).contains("\"id\":" + clinic.veterinarianId());
    assertThat(mine.body()).contains("\"id\":" + member.veterinarianId());
    assertThat(api.send(HttpMethod.GET, url, null, null).status()).isEqualTo(401);
    // Hoy cualquier usuario autenticado (incluso otra clínica) puede ver esta lista.
    // Si el equipo decide restringirla, agregar aquí: otra clínica -> 403.
  }

  @Test
  @DisplayName("A veterinarian profile is found by id and by IAM user id")
  void readsVeterinarian() throws Exception {
    var byId = api.send(HttpMethod.GET, "/api/v1/veterinarians/" + clinic.veterinarianId(),
        token(clinic), null);
    var byUser = api.send(HttpMethod.GET,
        "/api/v1/veterinarians?userId=" + clinic.holder().userId(), token(clinic), null);

    assertThat(byId.status()).isEqualTo(200);
    assertThat(byUser.status()).isEqualTo(200);
    assertThat(byUser.number("id")).isEqualTo(clinic.veterinarianId());
  }

  @Test
  @DisplayName("A veterinarian updates their own profile; another clinic cannot")
  void updatesVeterinarian() throws Exception {
    var url = "/api/v1/veterinarians/" + clinic.veterinarianId();
    var body = """
        {"firstName": "Ana", "lastName": "López", "phoneNumber": "987654321",
         "sex": "FEMALE", "licenseNumber": "CMVP 1234", "specialty": "Dermatología"}""";

    var mine = api.send(HttpMethod.PUT, url, token(clinic), body);
    var notMine = api.send(HttpMethod.PUT, url, token(otherClinic), body);

    assertThat(mine.status()).isEqualTo(200);
    assertThat(mine.body()).contains("\"specialty\":\"Dermatología\"");
    assertThat(notMine.status()).isEqualTo(403);
  }

  // ---------------------------------------------------------------- patients

  @Test
  @DisplayName("The clinic registers a pet as patient once; the second time is 409")
  void registersPatient() throws Exception {
    var url = "/api/v1/clinics/" + clinic.clinicId() + "/patients";
    var body = "{\"petId\": " + owner.petId() + "}";

    assertThat(api.send(HttpMethod.POST, url, token(clinic), body).status()).isEqualTo(201);
    assertThat(api.send(HttpMethod.POST, url, token(clinic), body).status()).isEqualTo(409);
  }

  @Test
  @DisplayName("Another clinic cannot register patients in my clinic, and an unknown pet is 404")
  void patientErrors() throws Exception {
    var url = "/api/v1/clinics/" + clinic.clinicId() + "/patients";

    assertThat(api.send(HttpMethod.POST, url, token(otherClinic),
        "{\"petId\": " + owner.petId() + "}").status()).isEqualTo(403);
    assertThat(api.send(HttpMethod.POST, url, token(clinic), "{\"petId\": 999999}").status())
        .isEqualTo(404);
  }

  @Test
  @DisplayName("Only the clinic sees its patients")
  void listsPatients() throws Exception {
    api.registerPatient(clinic, owner.petId());
    var url = "/api/v1/clinics/" + clinic.clinicId() + "/patients";

    var mine = api.send(HttpMethod.GET, url, token(clinic), null);
    assertThat(mine.status()).isEqualTo(200);
    assertThat(mine.body()).contains("\"id\":" + owner.petId());
    assertThat(api.send(HttpMethod.GET, url, token(otherClinic), null).status()).isEqualTo(403);
    assertThat(api.send(HttpMethod.GET, url, owner.owner().token(), null).status()).isEqualTo(403);
  }

  @Test
  @DisplayName("The owner sees the clinics of their pet; another owner cannot")
  void petClinics() throws Exception {
    api.registerPatient(clinic, owner.petId());
    var stranger = api.ownerWithPet();
    var url = "/api/v1/pets/" + owner.petId() + "/clinics";

    var mine = api.send(HttpMethod.GET, url, owner.owner().token(), null);
    assertThat(mine.status()).isEqualTo(200);
    assertThat(mine.body()).contains("\"id\":" + clinic.clinicId());
    assertThat(mine.body()).doesNotContain("\"id\":" + otherClinic.clinicId() + ",");
    assertThat(api.send(HttpMethod.GET, url, stranger.owner().token(), null).status())
        .isEqualTo(403);
  }

  @Test
  @DisplayName("A vet sees a pet only after it becomes a patient of the clinic")
  void vetSeesPatientPet() throws Exception {
    var url = "/api/v1/pets/" + owner.petId();

    assertThat(api.send(HttpMethod.GET, url, token(clinic), null).status()).isEqualTo(403);
    api.registerPatient(clinic, owner.petId());
    assertThat(api.send(HttpMethod.GET, url, token(clinic), null).status()).isEqualTo(200);
    assertThat(api.send(HttpMethod.GET, url, token(otherClinic), null).status()).isEqualTo(403);
  }
}

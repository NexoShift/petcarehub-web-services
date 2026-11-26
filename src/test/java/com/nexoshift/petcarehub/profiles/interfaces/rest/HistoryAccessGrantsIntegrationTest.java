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

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Permiso por horas: cada clínica ve lo suyo; lo de otras clínicas solo mientras el dueño
 * le da acceso (1 a 72 h) y hasta que lo revoca.
 */
@SpringBootTest(properties = "spring.profiles.active=test")
class HistoryAccessGrantsIntegrationTest {

  @Autowired
  private WebApplicationContext context;

  private PetCareApiClient api;
  private ClinicSetup clinicA;
  private ClinicSetup clinicB;
  private OwnerSetup owner;
  private long consultationOfA;
  private String grantsUrl;

  @BeforeEach
  void setUp() throws Exception {
    api = new PetCareApiClient(MockMvcBuilders.webAppContextSetup(context)
        .apply(SecurityMockMvcConfigurers.springSecurity()).build());
    clinicA = api.clinicWithVeterinarian();
    clinicB = api.clinicWithVeterinarian();
    owner = api.ownerWithPet();
    api.registerPatient(clinicA, owner.petId());
    api.registerPatient(clinicB, owner.petId());
    grantsUrl = "/api/v1/pets/" + owner.petId() + "/history-access-grants";

    var created = api.send(HttpMethod.POST, "/api/v1/clinical-consultations",
        clinicA.holder().token(), """
            {"petId": %d, "clinicId": %d, "reason": "Control en la clínica A"}"""
            .formatted(owner.petId(), clinicA.clinicId()));
    assertThat(created.status()).as(created.body()).isEqualTo(201);
    consultationOfA = created.number("id");
  }

  private String consultationsSeenBy(ClinicSetup clinic) throws Exception {
    var reply = api.send(HttpMethod.GET,
        "/api/v1/pets/" + owner.petId() + "/clinical-consultations", clinic.holder().token(), null);
    assertThat(reply.status()).as(reply.body()).isEqualTo(200);
    return reply.body();
  }

  private PetCareApiClient.Reply grant(String token, long clinicId, Integer hours)
      throws Exception {
    return api.send(HttpMethod.POST, grantsUrl, token,
        "{\"clinicId\": " + clinicId + ", \"hours\": " + hours + "}");
  }

  @Test
  @DisplayName("Without permission, another clinic does not see this clinic's consultations")
  void otherClinicSeesOnlyItsOwn() throws Exception {
    assertThat(consultationsSeenBy(clinicB)).doesNotContain("\"id\":" + consultationOfA + ",");
    assertThat(consultationsSeenBy(clinicA)).contains("\"id\":" + consultationOfA + ",");
    assertThat(api.send(HttpMethod.GET, "/api/v1/clinical-consultations/" + consultationOfA,
        clinicB.holder().token(), null).status()).isEqualTo(403);
  }

  @Test
  @DisplayName("The owner gives access for some hours, and then revokes it")
  void grantAndRevoke() throws Exception {
    var granted = grant(owner.owner().token(), clinicB.clinicId(), 2);
    assertThat(granted.status()).as(granted.body()).isEqualTo(201);
    assertThat(granted.body()).contains("\"status\":\"ACTIVE\"");

    assertThat(consultationsSeenBy(clinicB)).contains("\"id\":" + consultationOfA + ",");
    assertThat(api.send(HttpMethod.GET, "/api/v1/clinical-consultations/" + consultationOfA,
        clinicB.holder().token(), null).status()).isEqualTo(200);

    var revoked = api.send(HttpMethod.PUT, grantsUrl + "/" + granted.number("id") + "/revoke",
        owner.owner().token(), null);
    assertThat(revoked.status()).as(revoked.body()).isEqualTo(200);
    assertThat(revoked.body()).contains("\"status\":\"REVOKED\"");
    assertThat(consultationsSeenBy(clinicB)).doesNotContain("\"id\":" + consultationOfA + ",");
  }

  @Test
  @DisplayName("Giving access again renews the same grant")
  void grantAgainRenews() throws Exception {
    var first = grant(owner.owner().token(), clinicB.clinicId(), 1);
    var second = grant(owner.owner().token(), clinicB.clinicId(), 24);

    assertThat(second.number("id")).isEqualTo(first.number("id"));
    var list = api.send(HttpMethod.GET, grantsUrl, owner.owner().token(), null);
    assertThat(list.status()).isEqualTo(200);
    assertThat(list.body().split("\"id\":").length - 1).isEqualTo(1);
  }

  @Test
  @DisplayName("Hours go from 1 to 72, and only clinics where the pet is a patient")
  void validations() throws Exception {
    var stranger = api.clinicWithVeterinarian();

    assertThat(grant(owner.owner().token(), clinicB.clinicId(), 0).status()).isEqualTo(400);
    assertThat(grant(owner.owner().token(), clinicB.clinicId(), 73).status()).isEqualTo(400);
    assertThat(grant(owner.owner().token(), stranger.clinicId(), 2).status()).isEqualTo(409);
    assertThat(grant(owner.owner().token(), 999_999L, 2).status()).isEqualTo(404);
  }

  @Test
  @DisplayName("Only the owner gives access; a veterinarian sees only the grant of their clinic")
  void onlyTheOwner() throws Exception {
    var otherOwner = api.ownerWithPet();
    grant(owner.owner().token(), clinicA.clinicId(), 5);
    grant(owner.owner().token(), clinicB.clinicId(), 5);

    assertThat(grant(otherOwner.owner().token(), clinicB.clinicId(), 2).status()).isEqualTo(403);
    assertThat(grant(clinicB.holder().token(), clinicB.clinicId(), 2).status()).isEqualTo(403);

    var seenByB = api.send(HttpMethod.GET, grantsUrl, clinicB.holder().token(), null);
    assertThat(seenByB.status()).isEqualTo(200);
    assertThat(seenByB.body()).contains("\"clinicId\":" + clinicB.clinicId())
        .doesNotContain("\"clinicId\":" + clinicA.clinicId() + ",");
  }

  @Test
  @DisplayName("Medical history: vaccinations of another clinic need permission; allergies are always visible")
  void medicalHistory() throws Exception {
    var history = "/api/v1/pets/" + owner.petId() + "/medical-history";
    var vaccination = api.send(HttpMethod.POST, history + "/vaccinations", clinicA.holder().token(),
        """
        {"vaccineName": "Antirrábica", "doseNumber": 1, "appliedOn": "2026-10-01"}""");
    assertThat(vaccination.status()).as(vaccination.body()).isEqualTo(201);
    var allergy = api.send(HttpMethod.POST, history + "/allergies", owner.owner().token(), """
        {"allergen": "Penicilina", "severity": "SEVERE"}""");
    assertThat(allergy.status()).as(allergy.body()).isEqualTo(201);

    var withoutPermission = api.send(HttpMethod.GET, history, clinicB.holder().token(), null);
    assertThat(withoutPermission.body()).contains("Penicilina").doesNotContain("Antirrábica");

    grant(owner.owner().token(), clinicB.clinicId(), 3);
    var withPermission = api.send(HttpMethod.GET, history, clinicB.holder().token(), null);
    assertThat(withPermission.body()).contains("Penicilina", "Antirrábica");
  }
}

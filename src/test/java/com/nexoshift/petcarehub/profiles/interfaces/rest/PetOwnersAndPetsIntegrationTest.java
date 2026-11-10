package com.nexoshift.petcarehub.profiles.interfaces.rest;

import com.nexoshift.petcarehub.support.PetCareApiClient;
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
 * API de Profiles para dueños y mascotas, con IAM real y H2 (perfil test).
 * Escenario: "owner" con su mascota y "stranger", otro dueño con la suya.
 */
@SpringBootTest(properties = "spring.profiles.active=test")
class PetOwnersAndPetsIntegrationTest {

  @Autowired
  private WebApplicationContext context;

  private PetCareApiClient api;
  private OwnerSetup owner;
  private OwnerSetup stranger;

  @BeforeEach
  void setUp() throws Exception {
    api = new PetCareApiClient(MockMvcBuilders.webAppContextSetup(context)
        .apply(SecurityMockMvcConfigurers.springSecurity()).build());
    owner = api.ownerWithPet();
    stranger = api.ownerWithPet();
  }

  private String token(OwnerSetup who) {
    return who.owner().token();
  }

  // -------------------------------------------------------------- pet owners

  @Test
  @DisplayName("A pet owner cannot create a profile for another user")
  void ownerProfileForAnotherUser() throws Exception {
    var newcomer = api.signUpAndSignIn("PET_OWNER");

    var reply = api.sendForm("/api/v1/pet-owners", newcomer.token(), Map.of(
        "userId", owner.owner().userId(), "firstName", "X", "lastName", "Y",
        "email", "x@petcare.test", "phoneNumber", "912345678"));

    assertThat(reply.status()).isEqualTo(403);
  }

  @Test
  @DisplayName("A user can only have one pet owner profile")
  void ownerProfileTwice() throws Exception {
    var reply = api.sendForm("/api/v1/pet-owners", token(owner), Map.of(
        "userId", owner.owner().userId(), "firstName", "Ares", "lastName", "Torres",
        "email", "again@petcare.test", "phoneNumber", "912345678"));

    assertThat(reply.status()).isEqualTo(409);
  }

  @Test
  @DisplayName("A clinic account cannot create a pet owner profile")
  void clinicCannotBePetOwner() throws Exception {
    var clinic = api.signUpAndSignIn("CLINIC");

    var reply = api.sendForm("/api/v1/pet-owners", clinic.token(), Map.of(
        "userId", clinic.userId(), "firstName", "X", "lastName", "Y",
        "email", "c@petcare.test", "phoneNumber", "912345678"));

    assertThat(reply.status()).isEqualTo(403);
  }

  @Test
  @DisplayName("The owner reads their profile by id and by user id; another owner cannot")
  void readsOwnerProfile() throws Exception {
    var byId = "/api/v1/pet-owners/" + owner.petOwnerId();
    var byUser = "/api/v1/pet-owners?userId=" + owner.owner().userId();

    assertThat(api.send(HttpMethod.GET, byId, token(owner), null).status()).isEqualTo(200);
    assertThat(api.send(HttpMethod.GET, byUser, token(owner), null).status()).isEqualTo(200);
    assertThat(api.send(HttpMethod.GET, byId, token(stranger), null).status()).isEqualTo(403);
    assertThat(api.send(HttpMethod.GET, byUser, token(stranger), null).status()).isEqualTo(403);
  }

  @Test
  @DisplayName("The owner updates their profile; another owner cannot")
  void updatesOwnerProfile() throws Exception {
    var url = "/api/v1/pet-owners/" + owner.petOwnerId();
    var body = """
        {"firstName": "Giovany", "lastName": "Torres", "phoneNumber": "987654321", "sex": "MALE"}""";

    var mine = api.send(HttpMethod.PUT, url, token(owner), body);
    var notMine = api.send(HttpMethod.PUT, url, token(stranger), body);

    assertThat(mine.status()).isEqualTo(200);
    assertThat(mine.body()).contains("\"firstName\":\"Giovany\"");
    assertThat(notMine.status()).isEqualTo(403);
  }

  // -------------------------------------------------------------------- pets

  @Test
  @DisplayName("A new pet gets an id and a pet code")
  void createsPet() throws Exception {
    var reply = api.sendForm("/api/v1/pets", token(owner), Map.of(
        "ownerId", owner.petOwnerId(), "name", "Luna", "species", "CAT", "sex", "FEMALE"));

    assertThat(reply.status()).isEqualTo(201);
    assertThat(reply.text("petCode")).matches("PET-[A-HJ-NP-Z2-9]{8}");
  }

  @Test
  @DisplayName("Nobody can create a pet in the name of another owner")
  void petForAnotherOwner() throws Exception {
    var reply = api.sendForm("/api/v1/pets", token(stranger), Map.of(
        "ownerId", owner.petOwnerId(), "name", "Intruso", "species", "CAT"));

    assertThat(reply.status()).isEqualTo(403);
  }

  @Test
  @DisplayName("Invalid pet data is a 400")
  void invalidPet() throws Exception {
    var future = api.sendForm("/api/v1/pets", token(owner), Map.of(
        "ownerId", owner.petOwnerId(), "name", "Luna", "species", "CAT",
        "birthDate", java.time.LocalDate.now().plusDays(3).toString()));
    var withoutName = api.sendForm("/api/v1/pets", token(owner), Map.of(
        "ownerId", owner.petOwnerId(), "species", "CAT"));

    assertThat(future.status()).isEqualTo(400);
    assertThat(withoutName.status()).isEqualTo(400);
  }

  @Test
  @DisplayName("The owner reads, lists and updates their pet; another owner cannot")
  void petAccess() throws Exception {
    var url = "/api/v1/pets/" + owner.petId();
    var list = "/api/v1/pet-owners/" + owner.petOwnerId() + "/pets";
    var body = """
        {"name": "Spot Jr", "species": "DOG", "breed": "Pitbull", "sex": "MALE"}""";

    assertThat(api.send(HttpMethod.GET, url, token(owner), null).status()).isEqualTo(200);
    assertThat(api.send(HttpMethod.GET, list, token(owner), null).body()).contains("\"id\":" + owner.petId());
    var updated = api.send(HttpMethod.PUT, url, token(owner), body);
    assertThat(updated.status()).isEqualTo(200);
    assertThat(updated.body()).contains("\"name\":\"Spot Jr\"");

    assertThat(api.send(HttpMethod.GET, url, token(stranger), null).status()).isEqualTo(403);
    assertThat(api.send(HttpMethod.GET, list, token(stranger), null).status()).isEqualTo(403);
    assertThat(api.send(HttpMethod.PUT, url, token(stranger), body).status()).isEqualTo(403);
  }

  @Test
  @DisplayName("A pet that does not exist is 404")
  void unknownPet() throws Exception {
    assertThat(api.send(HttpMethod.GET, "/api/v1/pets/999999", token(owner), null).status())
        .isEqualTo(404);
  }

  @Test
  @DisplayName("Only clinic staff can search a pet by its code")
  void searchByCode() throws Exception {
    var code = api.send(HttpMethod.GET, "/api/v1/pets/" + owner.petId(), token(owner), null)
        .text("petCode");
    var clinic = api.clinicWithVeterinarian();
    var url = "/api/v1/pets?petCode=" + code;

    var asVet = api.send(HttpMethod.GET, url, clinic.holder().token(), null);
    assertThat(asVet.status()).isEqualTo(200);
    assertThat(asVet.body()).contains("\"id\":" + owner.petId());
    assertThat(api.send(HttpMethod.GET, url, token(stranger), null).status()).isEqualTo(403);
  }

  @Test
  @DisplayName("Without a token the profiles API answers 401")
  void requiresAuthentication() throws Exception {
    assertThat(api.send(HttpMethod.GET, "/api/v1/pets/" + owner.petId(), null, null).status())
        .isEqualTo(401);
  }
}

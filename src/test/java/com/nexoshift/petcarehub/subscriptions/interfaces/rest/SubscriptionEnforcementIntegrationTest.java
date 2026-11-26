package com.nexoshift.petcarehub.subscriptions.interfaces.rest;

import com.nexoshift.petcarehub.support.PetCareApiClient;
import com.nexoshift.petcarehub.support.PetCareApiClient.Session;
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
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Bloqueo sin suscripción: sin plan activo no hay mascotas, ni veterinarios, ni IA.
 * Con plan activo se respetan los límites (mascotas, vets) y se cuentan los créditos de IA.
 */
@SpringBootTest(properties = "spring.profiles.active=test")
class SubscriptionEnforcementIntegrationTest {

  @Autowired
  private WebApplicationContext context;

  private PetCareApiClient api;

  @BeforeEach
  void setUp() {
    api = new PetCareApiClient(MockMvcBuilders.webAppContextSetup(context)
        .apply(SecurityMockMvcConfigurers.springSecurity()).build());
  }

  private long petOwnerProfile(Session owner) throws Exception {
    var petOwner = api.sendForm("/api/v1/pet-owners", owner.token(), Map.of(
        "userId", owner.userId(),
        "firstName", "Ana",
        "lastName", "Ríos",
        "email", "owner" + owner.userId() + "@petcare.test",
        "phoneNumber", "912345678"));
    assertThat(petOwner.status()).as(petOwner.body()).isEqualTo(201);
    return petOwner.number("id");
  }

  private PetCareApiClient.Reply registerPet(Session owner, long petOwnerId, String name)
      throws Exception {
    return api.sendForm("/api/v1/pets", owner.token(), Map.of(
        "ownerId", petOwnerId,
        "name", name,
        "species", "CAT",
        "breed", "Mestizo",
        "birthDate", "2022-01-10",
        "sex", "FEMALE"));
  }

  private long clinicProfile(Session holder) throws Exception {
    var clinic = api.sendForm("/api/v1/clinics", holder.token(), Map.of(
        "accountId", holder.accountId(),
        "name", "Clínica " + holder.userId(),
        "ruc", "20" + (100_000_000L + holder.userId()),
        "phoneNumber", "987654321",
        "address", "Av. Arequipa 100"));
    assertThat(clinic.status()).as(clinic.body()).isEqualTo(201);
    return clinic.number("id");
  }

  private PetCareApiClient.Reply registerVeterinarian(Session holder, long clinicId)
      throws Exception {
    var email = "member-" + UUID.randomUUID() + "@petcare.test";
    var member = api.send(HttpMethod.POST, "/api/v1/accounts/" + holder.accountId() + "/users",
        holder.token(), "{\"email\": \"%s\", \"password\": \"%s\"}"
            .formatted(email, PetCareApiClient.PASSWORD));
    assertThat(member.status()).as(member.body()).isEqualTo(201);
    var userId = member.number("id");
    return api.sendForm("/api/v1/veterinarians", holder.token(), Map.of(
        "userId", userId,
        "clinicId", clinicId,
        "firstName", "Luis",
        "lastName", "Paz",
        "email", email,
        "licenseNumber", "CMVP " + userId));
  }

  @Test
  @DisplayName("Without an active subscription the owner cannot register pets (403)")
  void petWithoutSubscription() throws Exception {
    var owner = api.signUpAndSignIn("PET_OWNER");
    var petOwnerId = petOwnerProfile(owner);

    assertThat(registerPet(owner, petOwnerId, "Mishi").status()).isEqualTo(403);
  }

  @Test
  @DisplayName("A PENDING subscription (not paid yet) still blocks pets (403)")
  void petWithPendingSubscription() throws Exception {
    var owner = api.signUpAndSignIn("PET_OWNER");
    var petOwnerId = petOwnerProfile(owner);
    var pending = api.send(HttpMethod.POST,
        "/api/v1/accounts/" + owner.accountId() + "/subscriptions", owner.token(),
        "{\"planId\": " + api.planId("PET_OWNER_BASIC") + "}");
    assertThat(pending.status()).as(pending.body()).isEqualTo(201);

    assertThat(registerPet(owner, petOwnerId, "Mishi").status()).isEqualTo(403);
  }

  @Test
  @DisplayName("The basic owner plan allows 2 pets; the third one is rejected (409)")
  void petLimit() throws Exception {
    var owner = api.signUpAndSignIn("PET_OWNER");
    api.activateSubscription(owner, "PET_OWNER_BASIC");
    var petOwnerId = petOwnerProfile(owner);

    assertThat(registerPet(owner, petOwnerId, "Mishi").status()).isEqualTo(201);
    assertThat(registerPet(owner, petOwnerId, "Tom").status()).isEqualTo(201);
    var third = registerPet(owner, petOwnerId, "Garfield");

    assertThat(third.status()).as(third.body()).isEqualTo(409);
  }

  @Test
  @DisplayName("Without an active subscription the clinic cannot register veterinarians (403)")
  void veterinarianWithoutSubscription() throws Exception {
    var holder = api.signUpAndSignIn("CLINIC");
    var clinicId = clinicProfile(holder);

    assertThat(registerVeterinarian(holder, clinicId).status()).isEqualTo(403);
  }

  @Test
  @DisplayName("The basic clinic plan allows 4 veterinarians; the fifth one is rejected (409)")
  void veterinarianLimit() throws Exception {
    var holder = api.signUpAndSignIn("CLINIC");
    api.activateSubscription(holder, "CLINIC_BASIC");
    var clinicId = clinicProfile(holder);

    for (int i = 0; i < 4; i++) {
      var vet = registerVeterinarian(holder, clinicId);
      assertThat(vet.status()).as(vet.body()).isEqualTo(201);
    }
    var fifth = registerVeterinarian(holder, clinicId);

    assertThat(fifth.status()).as(fifth.body()).isEqualTo(409);
  }

  @Test
  @DisplayName("Each answer of the owner chat spends one LLM credit of the owner's plan")
  void ownerChatSpendsOneLlmCredit() throws Exception {
    var owner = api.ownerWithPet();
    var credits = "/api/v1/accounts/" + owner.owner().accountId() + "/subscription/ai-credits";

    var asked = api.send(HttpMethod.POST, "/api/v1/ai/owner-explanations", owner.owner().token(),
        "{\"petId\": %d, \"question\": \"Is it normal that my pet sleeps a lot?\"}"
            .formatted(owner.petId()));
    var balance = api.send(HttpMethod.GET, credits, owner.owner().token(), null);

    assertThat(asked.status()).as(asked.body()).isEqualTo(201);
    assertThat(balance.status()).as(balance.body()).isEqualTo(200);
    assertThat(balance.body()).contains(
        "\"llm\":{\"limit\":150,\"reserved\":0,\"consumed\":1,\"available\":149}");
  }

  @Test
  @DisplayName("Only the account holder can simulate the payment, and only with a subscription")
  void simulatedPaymentRules() throws Exception {
    var owner = api.signUpAndSignIn("PET_OWNER");
    var stranger = api.signUpAndSignIn("PET_OWNER");
    var url = "/api/v1/accounts/" + owner.accountId() + "/subscription/simulated-payments";

    assertThat(api.send(HttpMethod.POST, url, owner.token(), null).status()).isEqualTo(404);
    assertThat(api.send(HttpMethod.POST, url, stranger.token(), null).status()).isEqualTo(403);
  }
}

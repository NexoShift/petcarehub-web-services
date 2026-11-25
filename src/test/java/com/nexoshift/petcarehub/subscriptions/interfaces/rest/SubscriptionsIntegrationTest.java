package com.nexoshift.petcarehub.subscriptions.interfaces.rest;

import com.nexoshift.petcarehub.subscriptions.infrastructure.persistence.jpa.repositories.SubscriptionRepository;
import com.nexoshift.petcarehub.subscriptions.support.SubscriptionTestSupport;
import com.nexoshift.petcarehub.support.PetCareApiClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.web.context.WebApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * API de Subscriptions con IAM real y H2 (perfil test). Stripe tiene valores de prueba
 * que nunca llegan a Stripe: el pago se simula con {@link SubscriptionTestSupport}.
 * El checkout con Stripe se prueba en StripePaymentFlowIntegrationTest.
 */
@SpringBootTest(properties = "spring.profiles.active=test")
class SubscriptionsIntegrationTest {

  @Autowired
  private WebApplicationContext context;
  @Autowired
  private SubscriptionRepository subscriptionRepository;
  @Autowired
  private PlatformTransactionManager transactionManager;

  private PetCareApiClient api;
  private SubscriptionTestSupport subscriptions;

  @BeforeEach
  void setUp() {
    api = new PetCareApiClient(MockMvcBuilders.webAppContextSetup(context)
        .apply(SecurityMockMvcConfigurers.springSecurity()).build());
    subscriptions = new SubscriptionTestSupport(api, subscriptionRepository, transactionManager);
  }

  private String account(PetCareApiClient.Session session) {
    return "/api/v1/accounts/" + session.accountId();
  }

  // ------------------------------------------------------------------ plans

  @Test
  @DisplayName("Plans are public and the four default plans exist")
  void plansArePublic() throws Exception {
    var reply = api.send(HttpMethod.GET, "/api/v1/plans", null, null);

    assertThat(reply.status()).isEqualTo(200);
    assertThat(reply.body()).contains("PET_OWNER_BASIC", "PET_OWNER_PREMIUM",
        "CLINIC_BASIC", "CLINIC_PREMIUM");
  }

  @Test
  @DisplayName("Plans can be filtered by subscriber type")
  void plansBySubscriberType() throws Exception {
    var reply = api.send(HttpMethod.GET, "/api/v1/plans?subscriberType=CLINIC", null, null);

    assertThat(reply.status()).isEqualTo(200);
    assertThat(reply.body()).contains("CLINIC_BASIC").doesNotContain("PET_OWNER_BASIC");
  }

  @Test
  @DisplayName("An unknown plan answers 404")
  void unknownPlan() throws Exception {
    assertThat(api.send(HttpMethod.GET, "/api/v1/plans/999999", null, null).status())
        .isEqualTo(404);
  }

  // ----------------------------------------------------------- subscriptions

  @Test
  @DisplayName("A pet owner subscribes to an owner plan and it starts PENDING")
  void subscribeStartsPending() throws Exception {
    var owner = api.signUpAndSignIn("PET_OWNER");

    var reply = subscriptions.subscribe(owner, "PET_OWNER_BASIC");

    assertThat(reply.status()).as(reply.body()).isEqualTo(201);
    assertThat(reply.text("status")).isEqualTo("PENDING");
    assertThat(reply.text("planCode")).isEqualTo("PET_OWNER_BASIC");
  }

  @Test
  @DisplayName("A pet owner cannot take a clinic plan")
  void incompatiblePlan() throws Exception {
    var owner = api.signUpAndSignIn("PET_OWNER");

    assertThat(subscriptions.subscribe(owner, "CLINIC_BASIC").status()).isEqualTo(400);
  }

  @Test
  @DisplayName("An account can only have one subscription")
  void subscribeTwice() throws Exception {
    var clinic = api.signUpAndSignIn("CLINIC");
    subscriptions.subscribe(clinic, "CLINIC_BASIC");

    assertThat(subscriptions.subscribe(clinic, "CLINIC_PREMIUM").status()).isEqualTo(409);
  }

  @Test
  @DisplayName("Nobody can subscribe or read the subscription of another account")
  void otherAccount() throws Exception {
    var owner = api.signUpAndSignIn("PET_OWNER");
    var stranger = api.signUpAndSignIn("PET_OWNER");
    subscriptions.subscribe(owner, "PET_OWNER_BASIC");

    var create = api.send(HttpMethod.POST, account(owner) + "/subscriptions", stranger.token(),
        "{\"planId\": " + subscriptions.planId("PET_OWNER_BASIC") + "}");
    var read = api.send(HttpMethod.GET, account(owner) + "/subscription", stranger.token(), null);

    assertThat(create.status()).isEqualTo(403);
    assertThat(read.status()).isEqualTo(403);
  }

  @Test
  @DisplayName("Without a subscription, GET subscription answers 404")
  void noSubscription() throws Exception {
    var owner = api.signUpAndSignIn("PET_OWNER");

    assertThat(api.send(HttpMethod.GET, account(owner) + "/subscription", owner.token(), null)
        .status()).isEqualTo(404);
  }

  // ------------------------------------------------- entitlements and credits

  @Test
  @DisplayName("A PENDING subscription grants nothing yet")
  void pendingHasNoEntitlements() throws Exception {
    var owner = api.signUpAndSignIn("PET_OWNER");
    subscriptions.subscribe(owner, "PET_OWNER_BASIC");

    assertThat(api.send(HttpMethod.GET, account(owner) + "/subscription/entitlements",
        owner.token(), null).status()).isEqualTo(403);
    assertThat(api.send(HttpMethod.GET, account(owner) + "/subscription/ai-credits",
        owner.token(), null).status()).isEqualTo(403);
  }

  @Test
  @DisplayName("After payment the subscription is ACTIVE with the plan limits and credits")
  void activeSubscription() throws Exception {
    var clinic = api.signUpAndSignIn("CLINIC");
    subscriptions.activate(clinic, "CLINIC_BASIC");

    var subscription = api.send(HttpMethod.GET, account(clinic) + "/subscription",
        clinic.token(), null);
    var entitlements = api.send(HttpMethod.GET, account(clinic) + "/subscription/entitlements",
        clinic.token(), null);
    var credits = api.send(HttpMethod.GET, account(clinic) + "/subscription/ai-credits",
        clinic.token(), null);

    assertThat(subscription.text("status")).isEqualTo("ACTIVE");
    assertThat(entitlements.status()).as(entitlements.body()).isEqualTo(200);
    assertThat(entitlements.number("maxVeterinarians")).isEqualTo(4);
    assertThat(credits.status()).as(credits.body()).isEqualTo(200);
    assertThat(credits.body()).contains("\"limit\":100", "\"limit\":300");
  }

  // ---------------------------------------------------------------- webhook

  @Test
  @DisplayName("The Stripe webhook is public but rejects requests without a valid signature")
  void webhookWithoutSignature() throws Exception {
    var reply = api.send(HttpMethod.POST, "/api/v1/subscriptions/webhooks/stripe", null,
        "{\"id\": \"evt_fake\", \"type\": \"invoice.paid\"}");

    assertThat(reply.status()).isEqualTo(400);
  }
}

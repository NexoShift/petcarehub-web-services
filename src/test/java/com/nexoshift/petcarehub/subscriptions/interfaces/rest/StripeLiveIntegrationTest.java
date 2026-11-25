package com.nexoshift.petcarehub.subscriptions.interfaces.rest;

import com.nexoshift.petcarehub.subscriptions.infrastructure.persistence.jpa.repositories.SubscriptionRepository;
import com.nexoshift.petcarehub.subscriptions.support.SubscriptionTestSupport;
import com.nexoshift.petcarehub.support.PetCareApiClient;
import com.stripe.net.Webhook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.web.context.WebApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * Pruebas EN VIVO contra Stripe en modo test. Usan las variables de entorno reales
 * (STRIPE_SECRET_KEY y STRIPE_PRICE_*) y llaman a la API de Stripe.
 * <p>
 * Solo corren si STRIPE_SECRET_KEY empieza con sk_test_ (nunca con una clave live).
 * Sin esa variable aparecen como "skipped": así el CI y tus compañeros no las necesitan.
 */
@SpringBootTest(properties = {"spring.profiles.active=test",
    "stripe.webhook.secret=" + StripeLiveIntegrationTest.WEBHOOK_SECRET})
@EnabledIfEnvironmentVariable(named = "STRIPE_SECRET_KEY", matches = "sk_test_.+")
class StripeLiveIntegrationTest {

  /** Only for these tests: we sign our own events, so the real webhook secret is not needed. */
  static final String WEBHOOK_SECRET = "whsec_live_test_only";

  @Autowired
  private WebApplicationContext context;
  @Autowired
  private SubscriptionRepository subscriptionRepository;
  @Autowired
  private PlatformTransactionManager transactionManager;

  private MockMvc mockMvc;
  private PetCareApiClient api;
  private SubscriptionTestSupport subscriptions;

  @BeforeEach
  void setUp() {
    mockMvc = MockMvcBuilders.webAppContextSetup(context)
        .apply(SecurityMockMvcConfigurers.springSecurity()).build();
    api = new PetCareApiClient(mockMvc);
    subscriptions = new SubscriptionTestSupport(api, subscriptionRepository, transactionManager);
  }

  private PetCareApiClient.Reply checkout(PetCareApiClient.Session session) throws Exception {
    return api.send(HttpMethod.POST,
        "/api/v1/accounts/" + session.accountId() + "/subscription/checkout-sessions",
        session.token(), null);
  }

  @ParameterizedTest(name = "{1} has a valid Stripe price and opens a real checkout")
  @CsvSource({
      "PET_OWNER, PET_OWNER_BASIC, STRIPE_PRICE_PET_OWNER_BASIC",
      "PET_OWNER, PET_OWNER_PREMIUM, STRIPE_PRICE_PET_OWNER_PREMIUM",
      "CLINIC, CLINIC_BASIC, STRIPE_PRICE_CLINIC_BASIC",
      "CLINIC, CLINIC_PREMIUM, STRIPE_PRICE_CLINIC_PREMIUM"})
  @DisplayName("Each plan opens a real Stripe checkout with its configured price")
  void everyPlanOpensCheckout(String accountType, String planCode, String priceVariable)
      throws Exception {
    assumeTrue(System.getenv(priceVariable) != null, priceVariable + " is not set");
    var session = api.signUpAndSignIn(accountType);
    assertThat(subscriptions.subscribe(session, planCode).status()).isEqualTo(201);

    var reply = checkout(session);

    assertThat(reply.status()).as(planCode + ": " + reply.body()).isEqualTo(200);
    assertThat(reply.text("sessionId")).startsWith("cs_test_");
    assertThat(reply.text("checkoutUrl")).startsWith("https://checkout.stripe.com/");
  }

  @Test
  @DisplayName("Asking again returns the same open Stripe session")
  void sameSessionIsReused() throws Exception {
    assumeTrue(System.getenv("STRIPE_PRICE_PET_OWNER_BASIC") != null);
    var owner = api.signUpAndSignIn("PET_OWNER");
    subscriptions.subscribe(owner, "PET_OWNER_BASIC");

    var first = checkout(owner);
    var second = checkout(owner);

    assertThat(second.status()).isEqualTo(200);
    assertThat(second.text("sessionId")).isEqualTo(first.text("sessionId"));
  }

  @Test
  @DisplayName("A signed event of an unpaid checkout is checked against Stripe and does not activate")
  void unpaidCheckoutDoesNotActivate() throws Exception {
    assumeTrue(System.getenv("STRIPE_PRICE_PET_OWNER_BASIC") != null);
    var owner = api.signUpAndSignIn("PET_OWNER");
    subscriptions.subscribe(owner, "PET_OWNER_BASIC");
    var sessionId = checkout(owner).text("sessionId");
    var payload = """
        {"id": "evt_live_test_%s", "object": "event", "type": "checkout.session.completed",
         "data": {"object": {"id": "%s", "object": "checkout.session"}}}"""
        .formatted(sessionId, sessionId);
    var signature = Webhook.Signature.generateSignatureHeader(payload, WEBHOOK_SECRET,
        Webhook.Util.getTimeNow());

    var status = mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/subscriptions/webhooks/stripe")
            .contentType(MediaType.APPLICATION_JSON)
            .header("Stripe-Signature", signature)
            .content(payload))
        .andReturn().getResponse().getStatus();

    // The backend asks Stripe for the real session: it is still "open", so nothing is activated.
    assertThat(status).isEqualTo(503);
    var subscription = api.send(HttpMethod.GET,
        "/api/v1/accounts/" + owner.accountId() + "/subscription", owner.token(), null);
    assertThat(subscription.text("status")).isEqualTo("PENDING");
  }
}

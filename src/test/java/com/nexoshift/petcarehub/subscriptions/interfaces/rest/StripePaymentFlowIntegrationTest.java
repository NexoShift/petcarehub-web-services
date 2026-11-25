package com.nexoshift.petcarehub.subscriptions.interfaces.rest;

import com.nexoshift.petcarehub.subscriptions.application.internal.outboundservices.payments.CheckoutSessionData;
import com.nexoshift.petcarehub.subscriptions.application.internal.outboundservices.payments.PaymentGateway;
import com.nexoshift.petcarehub.subscriptions.application.internal.outboundservices.payments.PaymentWebhookData;
import com.nexoshift.petcarehub.subscriptions.application.internal.outboundservices.payments.PaymentWebhookGateway;
import com.nexoshift.petcarehub.subscriptions.application.internal.outboundservices.payments.SubscriptionPaymentData;
import com.nexoshift.petcarehub.subscriptions.domain.exceptions.InvalidWebhookSignatureException;
import com.nexoshift.petcarehub.subscriptions.domain.model.aggregates.Plan;
import com.nexoshift.petcarehub.subscriptions.domain.model.valueobjects.PlanCode;
import com.nexoshift.petcarehub.subscriptions.domain.model.valueobjects.SubscriptionPaymentPage;
import com.nexoshift.petcarehub.subscriptions.domain.model.valueobjects.SubscriptionStatus;
import com.nexoshift.petcarehub.subscriptions.infrastructure.persistence.jpa.repositories.SubscriptionRepository;
import com.nexoshift.petcarehub.subscriptions.support.SubscriptionTestSupport;
import com.nexoshift.petcarehub.support.PetCareApiClient;
import com.stripe.net.Webhook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.web.context.WebApplicationContext;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Flujo de pago completo por la API, con Stripe reemplazado por dobles de prueba.
 * <p>
 * Lo que se prueba es NUESTRO código: crear el checkout, recibir los webhooks firmados,
 * validar que el evento corresponde a la suscripción, activarla, no procesar dos veces el
 * mismo evento y pedir la cancelación. La firma se verifica con la librería real de Stripe.
 * Lo único falso es la red: los datos que Stripe devolvería se toman del propio evento.
 */
@SpringBootTest(properties = "spring.profiles.active=test")
class StripePaymentFlowIntegrationTest {

  static final String WEBHOOK_SECRET = "whsec_test_flow_secret";
  static final String WEBHOOK_URL = "/api/v1/subscriptions/webhooks/stripe";

  @Autowired
  private WebApplicationContext context;
  @Autowired
  private SubscriptionRepository subscriptionRepository;
  @Autowired
  private PlatformTransactionManager transactionManager;
  @Autowired
  private FakeStripe stripe;

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

  // --------------------------------------------------------------- helpers

  private String account(PetCareApiClient.Session session) {
    return "/api/v1/accounts/" + session.accountId();
  }

  /** Owner with a PENDING subscription and an open checkout session. */
  private Checkout ownerWithCheckout() throws Exception {
    var owner = api.signUpAndSignIn("PET_OWNER");
    assertThat(subscriptions.subscribe(owner, "PET_OWNER_BASIC").status()).isEqualTo(201);
    var reply = api.send(HttpMethod.POST, account(owner) + "/subscription/checkout-sessions",
        owner.token(), null);
    assertThat(reply.status()).as(reply.body()).isEqualTo(200);
    var created = stripe.sessionsOf(owner.accountId()).getFirst();
    return new Checkout(owner, reply.text("sessionId"), reply.text("checkoutUrl"), created);
  }

  private record Checkout(PetCareApiClient.Session owner, String sessionId, String checkoutUrl,
                          FakeStripe.CreatedSession created) {
  }

  private String checkoutCompletedEvent(String eventId, Checkout checkout, String planCode,
                                        String stripeSubscriptionId) {
    return """
        {"id": "%s", "object": "event", "type": "checkout.session.completed",
         "data": {"object": {"id": "%s", "object": "checkout.session",
           "subscription": "%s",
           "metadata": {"subscription_id": "%d", "account_id": "%d", "plan_code": "%s",
                        "checkout_attempt_id": "%s"}}}}"""
        .formatted(eventId, checkout.sessionId(), stripeSubscriptionId,
            checkout.created().subscriptionId(), checkout.owner().accountId(), planCode,
            checkout.created().attemptId());
  }

  private String invoiceEvent(String eventId, String type, Checkout checkout,
                              String stripeSubscriptionId) {
    return """
        {"id": "%s", "object": "event", "type": "%s",
         "data": {"object": {"id": "in_%s", "object": "invoice",
           "subscription": "%s",
           "metadata": {"subscription_id": "%d", "account_id": "%d", "plan_code": "PET_OWNER_BASIC",
                        "checkout_attempt_id": "%s"}}}}"""
        .formatted(eventId, type, UUID.randomUUID(), stripeSubscriptionId,
            checkout.created().subscriptionId(), checkout.owner().accountId(),
            checkout.created().attemptId());
  }

  private int postWebhook(String payload, String secret) throws Exception {
    var signature = Webhook.Signature.generateSignatureHeader(payload, secret,
        Webhook.Util.getTimeNow());
    return mockMvc.perform(MockMvcRequestBuilders.post(WEBHOOK_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .header("Stripe-Signature", signature)
            .content(payload))
        .andReturn().getResponse().getStatus();
  }

  private PetCareApiClient.Reply subscription(PetCareApiClient.Session owner) throws Exception {
    return api.send(HttpMethod.GET, account(owner) + "/subscription", owner.token(), null);
  }

  // ------------------------------------------------------------------ tests

  @Test
  @DisplayName("Checkout creates a Stripe session with the plan and the subscription in its metadata")
  void checkoutCreatesSession() throws Exception {
    var checkout = ownerWithCheckout();

    assertThat(checkout.checkoutUrl()).startsWith("https://checkout.stripe.com/");
    assertThat(checkout.created().planCode()).isEqualTo(PlanCode.PET_OWNER_BASIC);
    assertThat(checkout.created().accountId()).isEqualTo(checkout.owner().accountId());
    assertThat(subscription(checkout.owner()).text("status")).isEqualTo("PENDING");
  }

  @Test
  @DisplayName("Asking for checkout again reuses the open session instead of creating another")
  void checkoutIsReused() throws Exception {
    var checkout = ownerWithCheckout();

    var again = api.send(HttpMethod.POST,
        account(checkout.owner()) + "/subscription/checkout-sessions",
        checkout.owner().token(), null);

    assertThat(again.status()).isEqualTo(200);
    assertThat(again.text("sessionId")).isEqualTo(checkout.sessionId());
    assertThat(stripe.sessionsOf(checkout.owner().accountId())).hasSize(1);
  }

  @Test
  @DisplayName("After checkout.session.completed the subscription is ACTIVE and gets its AI credits")
  void paymentActivatesSubscription() throws Exception {
    var checkout = ownerWithCheckout();
    var stripeSubscription = "sub_" + UUID.randomUUID();
    stripe.willReport(stripeSubscription, checkout, SubscriptionStatus.ACTIVE);

    var status = postWebhook(checkoutCompletedEvent("evt_" + UUID.randomUUID(), checkout,
        "PET_OWNER_BASIC", stripeSubscription), WEBHOOK_SECRET);

    assertThat(status).isEqualTo(200);
    var subscription = subscription(checkout.owner());
    assertThat(subscription.text("status")).isEqualTo("ACTIVE");
    assertThat(subscription.body()).contains("currentPeriodEnd");
    var credits = api.send(HttpMethod.GET, account(checkout.owner()) + "/subscription/ai-credits",
        checkout.owner().token(), null);
    assertThat(credits.status()).as(credits.body()).isEqualTo(200);
  }

  @Test
  @DisplayName("Stripe retries: the same event twice is processed only once")
  void sameEventOnlyOnce() throws Exception {
    var checkout = ownerWithCheckout();
    var stripeSubscription = "sub_" + UUID.randomUUID();
    stripe.willReport(stripeSubscription, checkout, SubscriptionStatus.ACTIVE);
    var event = checkoutCompletedEvent("evt_" + UUID.randomUUID(), checkout, "PET_OWNER_BASIC",
        stripeSubscription);

    assertThat(postWebhook(event, WEBHOOK_SECRET)).isEqualTo(200);
    assertThat(postWebhook(event, WEBHOOK_SECRET)).isEqualTo(200);

    assertThat(stripe.lookups(stripeSubscription)).isEqualTo(1);
  }

  @Test
  @DisplayName("A failed renewal (invoice.payment_failed) leaves the subscription PAST_DUE")
  void failedRenewal() throws Exception {
    var checkout = ownerWithCheckout();
    var stripeSubscription = "sub_" + UUID.randomUUID();
    stripe.willReport(stripeSubscription, checkout, SubscriptionStatus.ACTIVE);
    postWebhook(checkoutCompletedEvent("evt_" + UUID.randomUUID(), checkout, "PET_OWNER_BASIC",
        stripeSubscription), WEBHOOK_SECRET);

    stripe.willReport(stripeSubscription, checkout, SubscriptionStatus.PAST_DUE);
    var status = postWebhook(invoiceEvent("evt_" + UUID.randomUUID(), "invoice.payment_failed",
        checkout, stripeSubscription), WEBHOOK_SECRET);

    assertThat(status).isEqualTo(200);
    assertThat(subscription(checkout.owner()).text("status")).isEqualTo("PAST_DUE");
  }

  @Test
  @DisplayName("An event for another plan than the subscription's is rejected and changes nothing")
  void eventForOtherPlanIsRejected() throws Exception {
    var checkout = ownerWithCheckout();
    var stripeSubscription = "sub_" + UUID.randomUUID();
    stripe.willReport(stripeSubscription, checkout, SubscriptionStatus.ACTIVE);

    var status = postWebhook(checkoutCompletedEvent("evt_" + UUID.randomUUID(), checkout,
        "PET_OWNER_PREMIUM", stripeSubscription), WEBHOOK_SECRET);

    assertThat(status).isEqualTo(503);
    assertThat(subscription(checkout.owner()).text("status")).isEqualTo("PENDING");
  }

  @Test
  @DisplayName("A webhook signed with another secret answers 400 and changes nothing")
  void forgedWebhookIsRejected() throws Exception {
    var checkout = ownerWithCheckout();
    var stripeSubscription = "sub_" + UUID.randomUUID();
    stripe.willReport(stripeSubscription, checkout, SubscriptionStatus.ACTIVE);

    var status = postWebhook(checkoutCompletedEvent("evt_" + UUID.randomUUID(), checkout,
        "PET_OWNER_BASIC", stripeSubscription), "whsec_attacker");

    assertThat(status).isEqualTo(400);
    assertThat(subscription(checkout.owner()).text("status")).isEqualTo("PENDING");
  }

  @Test
  @DisplayName("The holder schedules the cancellation at the end of the period in Stripe")
  void cancelAtPeriodEnd() throws Exception {
    var checkout = ownerWithCheckout();
    var stripeSubscription = "sub_" + UUID.randomUUID();
    stripe.willReport(stripeSubscription, checkout, SubscriptionStatus.ACTIVE);
    postWebhook(checkoutCompletedEvent("evt_" + UUID.randomUUID(), checkout, "PET_OWNER_BASIC",
        stripeSubscription), WEBHOOK_SECRET);

    var reply = api.send(HttpMethod.PATCH, account(checkout.owner()) + "/subscription",
        checkout.owner().token(), "{\"cancelAtPeriodEnd\": true}");

    assertThat(reply.status()).as(reply.body()).isEqualTo(202);
    assertThat(stripe.cancellations()).contains(stripeSubscription + "=true");
  }

  @Test
  @DisplayName("A subscription that was never paid cannot be cancelled in Stripe (409)")
  void cancelBeforePayment() throws Exception {
    var checkout = ownerWithCheckout();

    var reply = api.send(HttpMethod.PATCH, account(checkout.owner()) + "/subscription",
        checkout.owner().token(), "{\"cancelAtPeriodEnd\": true}");

    assertThat(reply.status()).isEqualTo(409);
  }

  // ------------------------------------------------------- Stripe test double

  @TestConfiguration
  static class FakeStripeConfiguration {

    @Bean
    FakeStripe fakeStripe() {
      return new FakeStripe();
    }

    @Bean
    @Primary
    PaymentGateway fakePaymentGateway(FakeStripe stripe) {
      return stripe.paymentGateway();
    }

    @Bean
    @Primary
    PaymentWebhookGateway fakePaymentWebhookGateway(FakeStripe stripe) {
      return stripe.webhookGateway();
    }
  }

  /**
   * Plays the role of Stripe. Checkout sessions live in memory, webhook signatures are checked
   * with the real Stripe library, and the data Stripe would return is read from the event.
   */
  static class FakeStripe {

    record CreatedSession(String sessionId, Long subscriptionId, Long accountId,
                          PlanCode planCode, String attemptId) {
    }

    private final Map<String, CheckoutSessionData> sessions = new ConcurrentHashMap<>();
    private final List<CreatedSession> created = new CopyOnWriteArrayList<>();
    private final Map<String, SubscriptionPaymentData> reports = new ConcurrentHashMap<>();
    private final Map<String, AtomicInteger> lookups = new ConcurrentHashMap<>();
    private final List<String> cancellations = new CopyOnWriteArrayList<>();
    private final JsonMapper json = JsonMapper.builder().build();

    List<CreatedSession> sessionsOf(long accountId) {
      return created.stream().filter(session -> session.accountId() == accountId).toList();
    }

    void willReport(String stripeSubscriptionId, Checkout checkout, SubscriptionStatus status) {
      var start = Instant.now().minus(1, ChronoUnit.HOURS);
      reports.put(stripeSubscriptionId, new SubscriptionPaymentData(
          checkout.created().subscriptionId(), checkout.owner().accountId(),
          checkout.created().planCode(), checkout.created().attemptId(),
          "cus_" + checkout.owner().accountId(), stripeSubscriptionId, status,
          start, start.plus(30, ChronoUnit.DAYS), false));
    }

    int lookups(String stripeSubscriptionId) {
      return lookups.getOrDefault(stripeSubscriptionId, new AtomicInteger()).get();
    }

    List<String> cancellations() {
      return cancellations;
    }

    PaymentGateway paymentGateway() {
      return new PaymentGateway() {
        @Override
        public CheckoutSessionData createCheckoutSession(Long subscriptionId, Long accountId,
                                                         Plan plan, String idempotencyKey) {
          var id = "cs_test_" + UUID.randomUUID();
          var data = new CheckoutSessionData(id, "https://checkout.stripe.com/c/pay/" + id,
              Instant.now().plus(1, ChronoUnit.HOURS), "open");
          sessions.put(id, data);
          created.add(new CreatedSession(id, subscriptionId, accountId, plan.getCode(),
              idempotencyKey));
          return data;
        }

        @Override
        public CheckoutSessionData retrieveCheckoutSession(String sessionId) {
          return sessions.get(sessionId);
        }

        @Override
        public void updateSubscriptionCancellation(String stripeSubscriptionId,
                                                   boolean cancelAtPeriodEnd) {
          cancellations.add(stripeSubscriptionId + "=" + cancelAtPeriodEnd);
        }

        @Override
        public SubscriptionPaymentPage retrieveSubscriptionPayments(String stripeSubscriptionId,
                                                                    int limit,
                                                                    String startingAfter) {
          return SubscriptionPaymentPage.empty();
        }
      };
    }

    PaymentWebhookGateway webhookGateway() {
      return new PaymentWebhookGateway() {
        @Override
        public Optional<PaymentWebhookData> verifyAndResolve(String payload, String signature) {
          JsonNode event;
          try {
            Webhook.constructEvent(payload, signature, WEBHOOK_SECRET);
            event = json.readTree(payload);
          } catch (Exception exception) {
            throw new InvalidWebhookSignatureException();
          }
          var type = event.path("type").stringValue();
          var object = event.path("data").path("object");
          var metadata = object.path("metadata");
          var isCheckout = type.startsWith("checkout.session.");
          var expiresAt = isCheckout ? sessions.get(object.path("id").stringValue()).expiresAt()
              : null;
          return Optional.of(new PaymentWebhookData(
              event.path("id").stringValue(), type,
              Long.valueOf(metadata.path("subscription_id").stringValue()),
              Long.valueOf(metadata.path("account_id").stringValue()),
              PlanCode.valueOf(metadata.path("plan_code").stringValue()),
              object.path("subscription").stringValue(),
              isCheckout ? null : object.path("id").stringValue(),
              isCheckout ? object.path("id").stringValue() : null,
              metadata.path("checkout_attempt_id").stringValue(),
              expiresAt));
        }

        @Override
        public SubscriptionPaymentData retrieveSubscriptionPaymentData(String stripeSubscriptionId,
                                                                      String invoiceId) {
          lookups.computeIfAbsent(stripeSubscriptionId, key -> new AtomicInteger())
              .incrementAndGet();
          return reports.get(stripeSubscriptionId);
        }

        @Override
        public boolean checkoutBelongsToSubscription(String checkoutSessionId,
                                                     String stripeSubscriptionId) {
          return true;
        }
      };
    }
  }
}

package com.nexoshift.petcarehub.subscriptions.support;

import com.nexoshift.petcarehub.subscriptions.domain.model.valueobjects.SubscriptionStatus;
import com.nexoshift.petcarehub.subscriptions.infrastructure.persistence.jpa.repositories.SubscriptionRepository;
import com.nexoshift.petcarehub.support.PetCareApiClient;
import org.springframework.http.HttpMethod;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Ayuda a los tests a tener cuentas con suscripción ACTIVA sin llamar a Stripe.
 * <p>
 * Crea la suscripción PENDING por la API (igual que el frontend) y luego simula el webhook
 * de pago llamando a {@code synchronizeBilling} dentro de una transacción. Así también se
 * dispara {@code SubscriptionPeriodPaidEvent}, que crea el periodo de créditos de IA.
 */
public final class SubscriptionTestSupport {

  private final PetCareApiClient api;
  private final SubscriptionRepository subscriptionRepository;
  private final TransactionTemplate transaction;

  public SubscriptionTestSupport(PetCareApiClient api,
                                 SubscriptionRepository subscriptionRepository,
                                 PlatformTransactionManager transactionManager) {
    this.api = api;
    this.subscriptionRepository = subscriptionRepository;
    this.transaction = new TransactionTemplate(transactionManager);
  }

  /** Id del plan por su código (PET_OWNER_BASIC, CLINIC_PREMIUM, ...), leído de GET /plans. */
  public long planId(String planCode) throws Exception {
    var plans = api.send(HttpMethod.GET, "/api/v1/plans", null, null);
    assertThat(plans.status()).as("plans " + plans.body()).isEqualTo(200);
    var matcher = Pattern.compile("\"id\":(\\d+),\"code\":\"" + planCode + "\"")
        .matcher(plans.body());
    assertThat(matcher.find()).as("plan " + planCode + " in " + plans.body()).isTrue();
    return Long.parseLong(matcher.group(1));
  }

  /** POST /accounts/{id}/subscriptions: deja la suscripción en PENDING. */
  public PetCareApiClient.Reply subscribe(PetCareApiClient.Session session, String planCode)
      throws Exception {
    return api.send(HttpMethod.POST,
        "/api/v1/accounts/" + session.accountId() + "/subscriptions", session.token(),
        "{\"planId\": " + planId(planCode) + "}");
  }

  /** Suscribe y marca el pago como hecho: suscripción ACTIVE por 30 días. */
  public void activate(PetCareApiClient.Session session, String planCode) throws Exception {
    var reply = subscribe(session, planCode);
    assertThat(reply.status()).as("subscribe " + reply.body()).isEqualTo(201);
    markPaid(session.accountId());
  }

  /** Simula el webhook de Stripe que confirma el pago del periodo actual. */
  public void markPaid(long accountId) {
    transaction.executeWithoutResult(status -> {
      var subscription = subscriptionRepository.findByAccountId(accountId).orElseThrow();
      var start = Instant.now().minus(1, ChronoUnit.DAYS);
      subscription.synchronizeBilling(
          "cus_test_" + accountId,
          "sub_test_" + UUID.randomUUID(),
          SubscriptionStatus.ACTIVE,
          start,
          start.plus(30, ChronoUnit.DAYS),
          false);
      subscriptionRepository.save(subscription);
    });
  }
}

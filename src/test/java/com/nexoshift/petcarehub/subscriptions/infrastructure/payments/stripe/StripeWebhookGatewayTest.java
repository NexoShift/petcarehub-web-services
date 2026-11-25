package com.nexoshift.petcarehub.subscriptions.infrastructure.payments.stripe;

import com.nexoshift.petcarehub.subscriptions.domain.exceptions.InvalidWebhookSignatureException;
import com.nexoshift.petcarehub.subscriptions.domain.exceptions.PaymentSynchronizationException;
import com.nexoshift.petcarehub.subscriptions.infrastructure.payments.stripe.configuration.StripeCheckoutProperties;
import com.stripe.net.Webhook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import tools.jackson.databind.json.JsonMapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Verificación de firmas del webhook con la librería real de Stripe, sin internet.
 * <p>
 * Stripe firma cada evento con HMAC-SHA256 usando el "signing secret" (whsec_...).
 * Aquí firmamos los eventos nosotros con un secreto de prueba, igual que lo haría Stripe.
 */
class StripeWebhookGatewayTest {

  private static final String SECRET = "whsec_test_secret_for_unit_tests";
  private static final String UNSUPPORTED_EVENT = """
      {"id": "evt_test_1", "object": "event", "type": "customer.created",
       "data": {"object": {"id": "cus_test_1", "object": "customer"}}}""";

  private StripeCheckoutProperties properties;
  private StripeWebhookGateway gateway;

  @BeforeEach
  void setUp() {
    properties = new StripeCheckoutProperties();
    ReflectionTestUtils.setField(properties, "webhookSecret", SECRET);
    gateway = new StripeWebhookGateway(properties, JsonMapper.builder().build());
  }

  private static String sign(String payload, String secret, long timestamp) throws Exception {
    return Webhook.Signature.generateSignatureHeader(payload, secret, timestamp);
  }

  private static long now() {
    return Webhook.Util.getTimeNow();
  }

  @Test
  @DisplayName("A correctly signed event is accepted (events the API does not use are ignored)")
  void validSignatureIsAccepted() throws Exception {
    var result = gateway.verifyAndResolve(UNSUPPORTED_EVENT, sign(UNSUPPORTED_EVENT, SECRET, now()));

    assertThat(result).isEmpty();
  }

  @Test
  @DisplayName("An event signed with another secret is rejected")
  void otherSecretIsRejected() throws Exception {
    var signature = sign(UNSUPPORTED_EVENT, "whsec_someone_else", now());

    assertThatThrownBy(() -> gateway.verifyAndResolve(UNSUPPORTED_EVENT, signature))
        .isInstanceOf(InvalidWebhookSignatureException.class);
  }

  @Test
  @DisplayName("An event whose body was changed after signing is rejected")
  void tamperedPayloadIsRejected() throws Exception {
    var signature = sign(UNSUPPORTED_EVENT, SECRET, now());
    var tampered = UNSUPPORTED_EVENT.replace("cus_test_1", "cus_attacker");

    assertThatThrownBy(() -> gateway.verifyAndResolve(tampered, signature))
        .isInstanceOf(InvalidWebhookSignatureException.class);
  }

  @Test
  @DisplayName("An old signature (replayed after 5 minutes) is rejected")
  void oldSignatureIsRejected() throws Exception {
    var signature = sign(UNSUPPORTED_EVENT, SECRET, now() - 600);

    assertThatThrownBy(() -> gateway.verifyAndResolve(UNSUPPORTED_EVENT, signature))
        .isInstanceOf(InvalidWebhookSignatureException.class);
  }

  @Test
  @DisplayName("A request without the Stripe-Signature header is rejected")
  void missingSignatureIsRejected() {
    assertThatThrownBy(() -> gateway.verifyAndResolve(UNSUPPORTED_EVENT, null))
        .isInstanceOf(InvalidWebhookSignatureException.class);
  }

  @Test
  @DisplayName("Without STRIPE_WEBHOOK_SECRET the webhook cannot be processed (503)")
  void missingSecretIsUnavailable() throws Exception {
    ReflectionTestUtils.setField(properties, "webhookSecret", "");
    var signature = sign(UNSUPPORTED_EVENT, SECRET, now());

    assertThatThrownBy(() -> gateway.verifyAndResolve(UNSUPPORTED_EVENT, signature))
        .isInstanceOf(PaymentSynchronizationException.class);
  }
}

package com.nexoshift.petcarehub;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Outside the tests the API only uses real services: the required settings have no default
 * values and the AI providers are the real ones.
 */
class ConfigurationDefaultsTest {

  private static Properties load(String file) throws IOException {
    var properties = new Properties();
    try (var in = ConfigurationDefaultsTest.class.getResourceAsStream("/" + file)) {
      properties.load(in);
    }
    return properties;
  }

  @Test
  @DisplayName("The AI uses OpenAI and Roboflow and the other contexts are always real")
  void realAiProviders() throws IOException {
    var common = load("application.properties");

    assertThat(common.getProperty("ai.llm.provider")).isEqualTo("${AI_LLM_PROVIDER:openai}");
    assertThat(common.getProperty("ai.vision.provider")).isEqualTo("${AI_VISION_PROVIDER:roboflow}");
    assertThat(common.getProperty("ai.acl.mode")).isNull();
    assertThat(load("application-prod.properties").getProperty("ai.llm.provider")).isNull();
  }

  @Test
  @DisplayName("Stripe, Google and the JWT secret have no default values")
  void requiredSecretsHaveNoDefaults() throws IOException {
    var common = load("application.properties");
    var prod = load("application-prod.properties");

    assertThat(common.getProperty("stripe.secret-key")).isEqualTo("${STRIPE_SECRET_KEY}");
    assertThat(common.getProperty("stripe.webhook.secret")).isEqualTo("${STRIPE_WEBHOOK_SECRET}");
    assertThat(common.getProperty("iam.google.client-ids")).isEqualTo("${GOOGLE_CLIENT_ID}");
    assertThat(prod.getProperty("authorization.jwt.secret")).isEqualTo("${JWT_SECRET}");
  }

  @Test
  @DisplayName("Simulated payments are off unless the variable turns them on, also in dev")
  void simulatedPaymentsOff() throws IOException {
    var off = "${SUBSCRIPTIONS_SIMULATED_PAYMENTS_ENABLED:false}";

    assertThat(load("application.properties").getProperty("subscriptions.simulated-payments.enabled"))
        .isEqualTo(off);
    assertThat(load("application-dev.properties").getProperty("subscriptions.simulated-payments.enabled"))
        .isEqualTo(off);
  }
}

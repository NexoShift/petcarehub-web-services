package com.nexoshift.petcarehub.subscriptions.infrastructure.payments.stripe.configuration;

import com.nexoshift.petcarehub.subscriptions.domain.model.valueobjects.PlanCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** The API must not start with missing or wrong Stripe settings. */
class StripeCheckoutPropertiesTest {

  private StripeCheckoutProperties properties;

  @BeforeEach
  void setUp() {
    properties = new StripeCheckoutProperties();
    set("secretKey", "sk_test_123");
    set("successUrl", "https://front.example/subscription?checkout=success");
    set("cancelUrl", "https://front.example/subscription?checkout=cancelled");
    set("petOwnerBasicPriceId", "price_owner_basic");
    set("petOwnerPremiumPriceId", "price_owner_premium");
    set("clinicBasicPriceId", "price_clinic_basic");
    set("clinicPremiumPriceId", "price_clinic_premium");
    set("webhookSecret", "whsec_123");
  }

  private void set(String field, String value) {
    ReflectionTestUtils.setField(properties, field, value);
  }

  @Test
  @DisplayName("Complete Stripe settings pass and each plan resolves its own price")
  void completeSettings() {
    assertThatCode(properties::afterPropertiesSet).doesNotThrowAnyException();
    assertThat(properties.resolvePriceId(PlanCode.PET_OWNER_BASIC)).isEqualTo("price_owner_basic");
    assertThat(properties.resolvePriceId(PlanCode.CLINIC_PREMIUM)).isEqualTo("price_clinic_premium");
  }

  @Test
  @DisplayName("An empty secret key stops the startup and names the variable")
  void emptySecretKey() {
    set("secretKey", " ");

    assertThatThrownBy(properties::afterPropertiesSet)
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("STRIPE_SECRET_KEY is empty");
  }

  @Test
  @DisplayName("Every wrong value is reported at once")
  void reportsEveryProblem() {
    set("petOwnerPremiumPriceId", "");
    set("webhookSecret", "secret");
    set("cancelUrl", "localhost:4200");

    assertThatThrownBy(properties::afterPropertiesSet)
        .hasMessageContaining("STRIPE_PRICE_PET_OWNER_PREMIUM is empty")
        .hasMessageContaining("STRIPE_WEBHOOK_SECRET must start with whsec_")
        .hasMessageContaining("STRIPE_CHECKOUT_CANCEL_URL must start with http:// or https://");
  }

  @Test
  @DisplayName("A publishable key (pk_) is rejected as secret key")
  void publishableKeyIsRejected() {
    set("secretKey", "pk_test_123");

    assertThatThrownBy(properties::afterPropertiesSet)
        .hasMessageContaining("STRIPE_SECRET_KEY must start with sk_ or rk_");
  }

  @Test
  @DisplayName("A restricted key (rk_) is accepted")
  void restrictedKeyIsAccepted() {
    set("secretKey", "rk_test_123");

    assertThatCode(properties::afterPropertiesSet).doesNotThrowAnyException();
  }
}

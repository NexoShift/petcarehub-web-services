package com.nexoshift.petcarehub.subscriptions.domain;

import com.nexoshift.petcarehub.subscriptions.domain.exceptions.AiCreditLimitExceededException;
import com.nexoshift.petcarehub.subscriptions.domain.exceptions.InvalidAiCreditReservationStateException;
import com.nexoshift.petcarehub.subscriptions.domain.exceptions.PaymentSynchronizationException;
import com.nexoshift.petcarehub.subscriptions.domain.exceptions.RegistrationLimitExceededException;
import com.nexoshift.petcarehub.subscriptions.domain.model.aggregates.AiCreditPeriod;
import com.nexoshift.petcarehub.subscriptions.domain.model.aggregates.Plan;
import com.nexoshift.petcarehub.subscriptions.domain.model.aggregates.Subscription;
import com.nexoshift.petcarehub.subscriptions.domain.model.commands.CreateSubscriptionCommand;
import com.nexoshift.petcarehub.subscriptions.domain.model.valueobjects.AiCreditType;
import com.nexoshift.petcarehub.subscriptions.domain.model.valueobjects.PlanCode;
import com.nexoshift.petcarehub.subscriptions.domain.model.valueobjects.PlanLimits;
import com.nexoshift.petcarehub.subscriptions.domain.model.valueobjects.SubscriptionStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SubscriptionsDomainTest {

  private static Plan plan(PlanCode code, PlanLimits limits) {
    var plan = new Plan(code, code.name(), new BigDecimal("10.00"), limits);
    ReflectionTestUtils.setField(plan, "id", 1L);
    return plan;
  }

  private static Subscription pending(Plan plan) {
    var subscription = new Subscription(new CreateSubscriptionCommand(7L, 1L), plan);
    ReflectionTestUtils.setField(subscription, "id", 3L);
    return subscription;
  }

  private static Subscription active(Plan plan) {
    var subscription = pending(plan);
    var start = Instant.now().minus(1, ChronoUnit.DAYS);
    subscription.synchronizeBilling("cus_1", "sub_1", SubscriptionStatus.ACTIVE,
        start, start.plus(30, ChronoUnit.DAYS), false);
    return subscription;
  }

  private static Plan ownerBasic() {
    return plan(PlanCode.PET_OWNER_BASIC, new PlanLimits(2, null, 1, 2));
  }

  @Test
  @DisplayName("Plan limits need exactly one of max pets or max veterinarians")
  void planLimitsNeedOneResource() {
    assertThatThrownBy(() -> new PlanLimits(2, 3, 1, 1))
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> new PlanLimits(null, null, 1, 1))
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> new PlanLimits(0, null, 1, 1))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  @DisplayName("Registration limit allows below the maximum and rejects at the maximum")
  void registrationLimit() {
    var limits = new PlanLimits(2, null, 1, 1);

    limits.ensurePetRegistrationAllowed(1);
    assertThatThrownBy(() -> limits.ensurePetRegistrationAllowed(2))
        .isInstanceOf(RegistrationLimitExceededException.class);
  }

  @Test
  @DisplayName("A new subscription is PENDING and not active")
  void newSubscriptionIsPending() {
    var subscription = pending(ownerBasic());

    assertThat(subscription.getStatus()).isEqualTo(SubscriptionStatus.PENDING);
    assertThat(subscription.isActive()).isFalse();
  }

  @Test
  @DisplayName("A paid period activates the subscription")
  void paidPeriodActivates() {
    assertThat(active(ownerBasic()).isActive()).isTrue();
  }

  @Test
  @DisplayName("Billing for an expired period leaves the subscription PAST_DUE")
  void expiredPeriodIsPastDue() {
    var subscription = pending(ownerBasic());
    var start = Instant.now().minus(40, ChronoUnit.DAYS);

    subscription.synchronizeBilling("cus_1", "sub_1", SubscriptionStatus.ACTIVE,
        start, start.plus(30, ChronoUnit.DAYS), false);

    assertThat(subscription.getStatus()).isEqualTo(SubscriptionStatus.PAST_DUE);
    assertThat(subscription.isActive()).isFalse();
  }

  @Test
  @DisplayName("Billing cannot switch to another Stripe subscription")
  void billingCannotChangeStripeSubscription() {
    var subscription = active(ownerBasic());
    var start = Instant.now();

    assertThatThrownBy(() -> subscription.synchronizeBilling("cus_1", "sub_other",
        SubscriptionStatus.ACTIVE, start, start.plus(30, ChronoUnit.DAYS), false))
        .isInstanceOf(PaymentSynchronizationException.class);
  }

  @Test
  @DisplayName("Credits: reserve, confirm and release keep the balance right")
  void creditLifecycle() {
    var period = new AiCreditPeriod(active(ownerBasic()));

    period.reserve(AiCreditType.LLM);
    period.reserve(AiCreditType.LLM);
    assertThat(period.getLlmAvailable()).isZero();

    period.confirm(AiCreditType.LLM);
    period.release(AiCreditType.LLM);

    assertThat(period.getLlmConsumed()).isEqualTo(1);
    assertThat(period.getLlmReserved()).isZero();
    assertThat(period.getLlmAvailable()).isEqualTo(1);
  }

  @Test
  @DisplayName("Credits: no reservation beyond the monthly limit")
  void creditLimit() {
    var period = new AiCreditPeriod(active(ownerBasic()));
    period.reserve(AiCreditType.COMPUTER_VISION);

    assertThatThrownBy(() -> period.reserve(AiCreditType.COMPUTER_VISION))
        .isInstanceOf(AiCreditLimitExceededException.class);
  }

  @Test
  @DisplayName("Credits: confirming without a reservation is rejected")
  void confirmWithoutReservation() {
    var period = new AiCreditPeriod(active(ownerBasic()));

    assertThatThrownBy(() -> period.confirm(AiCreditType.COMPUTER_VISION))
        .isInstanceOf(InvalidAiCreditReservationStateException.class);
  }
}

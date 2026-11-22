package com.nexoshift.petcarehub.subscriptions.domain.model.aggregates;

import com.nexoshift.petcarehub.shared.domain.model.aggregates.AuditableAbstractAggregateRoot;
import com.nexoshift.petcarehub.subscriptions.domain.exceptions.InvalidSubscriptionStatusException;
import com.nexoshift.petcarehub.subscriptions.domain.exceptions.PaymentSynchronizationException;
import com.nexoshift.petcarehub.subscriptions.domain.exceptions.PlanNotAvailableException;
import com.nexoshift.petcarehub.subscriptions.domain.model.commands.CreateSubscriptionCommand;
import com.nexoshift.petcarehub.subscriptions.domain.model.events.SubscriptionPeriodPaidEvent;
import com.nexoshift.petcarehub.subscriptions.domain.model.valueobjects.SubscriptionStatus;
import jakarta.persistence.*;
import lombok.Getter;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import java.util.UUID;

/**
 * Aggregate root representing an account subscription.
 */
@Getter
@Entity
@Table(name = "subscriptions")
public class Subscription
        extends AuditableAbstractAggregateRoot<Subscription> {

    @Column(name = "account_id", nullable = false, unique = true)
    private Long accountId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "plan_id", nullable = false)
    private Plan plan;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SubscriptionStatus status;

    @Column(name = "stripe_checkout_session_id", unique = true)
    private String stripeCheckoutSessionId;

    @Column(name = "stripe_checkout_url", columnDefinition = "TEXT")
    private String stripeCheckoutUrl;

    @Column(name = "checkout_expires_at")
    private Instant checkoutExpiresAt;

    @Column(name = "stripe_customer_id")
    private String stripeCustomerId;

    @Column(name = "stripe_subscription_id", unique = true)
    private String stripeSubscriptionId;

    @Column(name = "current_period_start")
    private Instant currentPeriodStart;

    @Column(name = "current_period_end")
    private Instant currentPeriodEnd;

    @Column(name = "cancel_at_period_end", nullable = false)
    private boolean cancelAtPeriodEnd;

    @Column(length = 36, unique = true)
    private String checkoutAttemptId;

    private Instant checkoutAttemptStartedAt;

    protected Subscription() {
        super();
    }

    /**
     * Creates a subscription awaiting payment confirmation.
     *
     * @param command the create subscription command
     * @param plan the selected subscription plan
     */
    public Subscription(
            CreateSubscriptionCommand command,
            Plan plan) {

        this();

        if (command == null || plan == null) {
            throw new IllegalArgumentException(
                    "Subscription command and plan are required");
        }

        if (!command.planId().equals(plan.getId())) {
            throw new IllegalArgumentException(
                    "The selected plan does not match the subscription command");
        }

        if (!plan.isActive()) {
            throw new PlanNotAvailableException(plan.getId());
        }

        this.accountId = command.accountId();
        this.plan = plan;
        this.status = SubscriptionStatus.PENDING;
        this.cancelAtPeriodEnd = false;
    }

    public void assignCheckoutSession(
            String sessionId,
            String checkoutUrl,
            Instant expiresAt) {

        if (status != SubscriptionStatus.PENDING) {
            throw new InvalidSubscriptionStatusException();
        }

        if (sessionId == null || sessionId.isBlank()) {
            throw new IllegalArgumentException(
                    "Checkout session ID is required");
        }

        if (expiresAt == null) {
            throw new IllegalArgumentException(
                    "Checkout expiration date is required");
        }

        this.stripeCheckoutSessionId = sessionId;
        this.stripeCheckoutUrl = checkoutUrl;
        this.checkoutExpiresAt = expiresAt;
    }

    /**
     * Checks whether the existing Checkout session can be reused.
     */
    public boolean hasAvailableCheckout() {

        return status == SubscriptionStatus.PENDING
                && stripeCheckoutSessionId != null
                && stripeCheckoutUrl != null
                && !stripeCheckoutUrl.isBlank()
                && checkoutExpiresAt != null
                && checkoutExpiresAt.isAfter(Instant.now());
    }

    /**
     * Checks whether the subscription already grants paid access.
     */
    public boolean isActive() {

        Instant now = Instant.now();

        return status == SubscriptionStatus.ACTIVE
                && currentPeriodStart != null
                && currentPeriodEnd != null
                && !currentPeriodStart.isAfter(now)
                && currentPeriodEnd.isAfter(now);
    }

    public void startCheckoutAttempt() {

        if (status != SubscriptionStatus.PENDING) {
            throw new InvalidSubscriptionStatusException();
        }

        this.checkoutAttemptId = UUID.randomUUID().toString();
        this.checkoutAttemptStartedAt = Instant.now();

        this.stripeCheckoutSessionId = null;
        this.stripeCheckoutUrl = null;
        this.checkoutExpiresAt = null;
    }

    public boolean hasCheckoutAttempt() {
        return checkoutAttemptId != null;
    }

    public boolean canRetryCheckoutAttempt() {
        return checkoutAttemptStartedAt != null
                && Instant.now().isBefore(
                checkoutAttemptStartedAt.plus(23, ChronoUnit.HOURS));
    }

    public void synchronizeBilling(
            String customerId,
            String subscriptionId,
            SubscriptionStatus providerStatus,
            Instant paidPeriodStart,
            Instant paidPeriodEnd,
            boolean cancelAtPeriodEnd) {

        if (customerId == null || customerId.isBlank()
                || subscriptionId == null || subscriptionId.isBlank()
                || providerStatus == null) {

            throw new PaymentSynchronizationException();
        }

        if (stripeSubscriptionId != null
                && !Objects.equals(
                stripeSubscriptionId, subscriptionId)) {

            throw new PaymentSynchronizationException();
        }

        if (stripeCustomerId != null
                && !Objects.equals(stripeCustomerId, customerId)) {

            throw new PaymentSynchronizationException();
        }

        if ((paidPeriodStart == null) != (paidPeriodEnd == null)) {
            throw new PaymentSynchronizationException();
        }

        if (paidPeriodStart != null
                && !paidPeriodEnd.isAfter(paidPeriodStart)) {

            throw new PaymentSynchronizationException();
        }

        boolean wasActive = isActive();

        this.stripeCustomerId = customerId;
        this.stripeSubscriptionId = subscriptionId;
        this.cancelAtPeriodEnd = cancelAtPeriodEnd;

        boolean periodAdvanced = paidPeriodEnd != null
                && (currentPeriodEnd == null
                || paidPeriodEnd.isAfter(currentPeriodEnd));

        if (periodAdvanced) {

            this.currentPeriodStart = paidPeriodStart;
            this.currentPeriodEnd = paidPeriodEnd;
        }

        if (providerStatus == SubscriptionStatus.ACTIVE) {

            if (currentPeriodEnd == null) {

                this.status = SubscriptionStatus.PENDING;

            } else if (!currentPeriodEnd.isAfter(Instant.now())) {

                this.status = SubscriptionStatus.PAST_DUE;

            } else {

                this.status = SubscriptionStatus.ACTIVE;
            }

        } else {

            this.status = providerStatus;
        }

        if (isActive() && (periodAdvanced || !wasActive)) {

            registerEvent(new SubscriptionPeriodPaidEvent(
                    getId(),
                    accountId,
                    currentPeriodStart,
                    currentPeriodEnd
            ));
        }
    }

    public void recordCompletedCheckout(
            String sessionId,
            Instant expiresAt) {

        if (sessionId == null || sessionId.isBlank()
                || expiresAt == null) {

            throw new PaymentSynchronizationException();
        }

        if (stripeCheckoutSessionId != null
                && !stripeCheckoutSessionId.equals(sessionId)) {

            throw new PaymentSynchronizationException();
        }

        this.stripeCheckoutSessionId = sessionId;
        this.checkoutExpiresAt = expiresAt;
        this.stripeCheckoutUrl = null;
    }

}
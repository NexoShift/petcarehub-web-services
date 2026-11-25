package com.nexoshift.petcarehub.subscriptions.application.internal.outboundservices.payments;

import com.nexoshift.petcarehub.subscriptions.domain.model.valueobjects.PlanCode;

import java.time.Instant;

public record PaymentWebhookData(
        String eventId,
        String eventType,
        Long subscriptionId,
        Long accountId,
        PlanCode planCode,
        String stripeSubscriptionId,
        String invoiceId,
        String checkoutSessionId,
        String checkoutAttemptId,
        Instant checkoutExpiresAt
) {

    public boolean isCheckoutExpired() {
        return "checkout.session.expired".equals(eventType);
    }

    public boolean isCheckoutCompleted() {
        return "checkout.session.completed".equals(eventType);
    }
}
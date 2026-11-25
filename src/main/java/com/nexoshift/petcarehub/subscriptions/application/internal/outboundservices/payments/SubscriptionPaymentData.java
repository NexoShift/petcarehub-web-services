package com.nexoshift.petcarehub.subscriptions.application.internal.outboundservices.payments;

import com.nexoshift.petcarehub.subscriptions.domain.model.valueobjects.PlanCode;
import com.nexoshift.petcarehub.subscriptions.domain.model.valueobjects.SubscriptionStatus;

import java.time.Instant;

public record SubscriptionPaymentData(
        Long subscriptionId,
        Long accountId,
        PlanCode planCode,
        String checkoutAttemptId,
        String stripeCustomerId,
        String stripeSubscriptionId,
        SubscriptionStatus status,
        Instant paidPeriodStart,
        Instant paidPeriodEnd,
        boolean cancelAtPeriodEnd
) {}
package com.nexoshift.petcarehub.subscriptions.interfaces.rest.resources;

import com.nexoshift.petcarehub.subscriptions.domain.model.valueobjects.PlanCode;
import com.nexoshift.petcarehub.subscriptions.domain.model.valueobjects.SubscriptionStatus;

import java.time.Instant;

/**
 * Resource representing an account subscription.
 */
public record SubscriptionResource(
        Long id,
        Long accountId,
        Long planId,
        PlanCode planCode,
        SubscriptionStatus status,
        Instant currentPeriodStart,
        Instant currentPeriodEnd,
        boolean cancelAtPeriodEnd
) {
}
package com.nexoshift.petcarehub.subscriptions.interfaces.rest.resources;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * Resource used to create a subscription.
 */
public record CreateSubscriptionResource(
        @NotNull
        @Positive
        Long planId
) {
}
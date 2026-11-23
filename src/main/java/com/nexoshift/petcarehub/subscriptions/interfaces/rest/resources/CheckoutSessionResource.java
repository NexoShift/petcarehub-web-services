package com.nexoshift.petcarehub.subscriptions.interfaces.rest.resources;

import java.time.Instant;

public record CheckoutSessionResource(
        Long subscriptionId,
        String sessionId,
        String checkoutUrl,
        Instant expiresAt
) {}
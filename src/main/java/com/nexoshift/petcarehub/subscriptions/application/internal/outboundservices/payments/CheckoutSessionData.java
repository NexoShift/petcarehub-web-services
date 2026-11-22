package com.nexoshift.petcarehub.subscriptions.application.internal.outboundservices.payments;

import java.time.Instant;

public record CheckoutSessionData(
        String sessionId,
        String checkoutUrl,
        Instant expiresAt,
        String status
) {

    public boolean isOpen() {
        return "open".equals(status);
    }

    public boolean isCompleted() {
        return "complete".equals(status);
    }

    public boolean isExpired() {
        return "expired".equals(status);
    }
}
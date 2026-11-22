package com.nexoshift.petcarehub.subscriptions.interfaces.rest.resources;

import java.math.BigDecimal;
import java.time.Instant;

public record SubscriptionPaymentResource(
        String invoiceId,
        String invoiceNumber,
        BigDecimal amountPaid,
        String currency,
        String status,
        Instant paidAt,
        String invoiceUrl,
        String invoicePdfUrl
) {
}
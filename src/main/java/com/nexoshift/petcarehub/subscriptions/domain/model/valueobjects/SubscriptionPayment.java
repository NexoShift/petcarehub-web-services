package com.nexoshift.petcarehub.subscriptions.domain.model.valueobjects;

import java.math.BigDecimal;
import java.time.Instant;

public record SubscriptionPayment(
        String invoiceId,
        String invoiceNumber,
        BigDecimal amountPaid,
        String currency,
        Instant paidAt,
        String invoiceUrl,
        String invoicePdfUrl
) {
}
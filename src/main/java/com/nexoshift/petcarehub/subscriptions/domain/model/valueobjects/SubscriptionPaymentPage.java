package com.nexoshift.petcarehub.subscriptions.domain.model.valueobjects;

import java.util.List;

public record SubscriptionPaymentPage(
        List<SubscriptionPayment> payments,
        String nextCursor
) {

    public SubscriptionPaymentPage {
        payments = List.copyOf(payments);
    }

    public boolean hasMore() {
        return nextCursor != null;
    }

    public static SubscriptionPaymentPage empty() {
        return new SubscriptionPaymentPage(List.of(), null);
    }
}
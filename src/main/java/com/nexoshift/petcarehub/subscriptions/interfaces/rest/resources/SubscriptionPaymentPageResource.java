package com.nexoshift.petcarehub.subscriptions.interfaces.rest.resources;

import java.util.List;

public record SubscriptionPaymentPageResource(
        List<SubscriptionPaymentResource> payments,
        boolean hasMore,
        String nextCursor
) {
}
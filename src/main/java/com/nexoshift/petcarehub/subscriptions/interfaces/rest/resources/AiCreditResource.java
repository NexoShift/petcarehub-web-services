package com.nexoshift.petcarehub.subscriptions.interfaces.rest.resources;

import java.time.Instant;

public record AiCreditResource(
        Long subscriptionId,
        Long accountId,
        Instant periodStart,
        Instant periodEnd,
        CreditBalance computerVision,
        CreditBalance llm
) {

    public record CreditBalance(
            int limit,
            int reserved,
            int consumed,
            int available
    ) {}
}
package com.nexoshift.petcarehub.subscriptions.domain.model.events;

import java.time.Instant;

public record SubscriptionPeriodPaidEvent(
        Long subscriptionId,
        Long accountId,
        Instant periodStart,
        Instant periodEnd
) {}
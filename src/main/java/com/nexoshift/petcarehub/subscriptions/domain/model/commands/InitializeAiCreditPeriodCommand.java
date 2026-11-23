package com.nexoshift.petcarehub.subscriptions.domain.model.commands;

import java.time.Instant;

public record InitializeAiCreditPeriodCommand(
        Long subscriptionId,
        Long accountId,
        Instant periodStart,
        Instant periodEnd
) {}
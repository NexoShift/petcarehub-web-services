package com.nexoshift.petcarehub.subscriptions.domain.model.commands;

public record ProcessPaymentWebhookCommand(
        String payload,
        String signature
) {}
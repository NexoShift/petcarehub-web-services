package com.nexoshift.petcarehub.subscriptions.domain.model.commands;

public record CreateCheckoutSessionCommand(Long accountId) {

    public CreateCheckoutSessionCommand {
        if (accountId == null || accountId <= 0) {
            throw new IllegalArgumentException(
                    "Account ID must be positive");
        }
    }
}
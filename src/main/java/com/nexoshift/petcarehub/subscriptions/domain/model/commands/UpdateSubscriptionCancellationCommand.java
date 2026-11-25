package com.nexoshift.petcarehub.subscriptions.domain.model.commands;

public record UpdateSubscriptionCancellationCommand(
        Long accountId,
        Boolean cancelAtPeriodEnd
) {

    public UpdateSubscriptionCancellationCommand {

        if (accountId == null || accountId <= 0) {
            throw new IllegalArgumentException(
                    "Account ID must be a positive number."
            );
        }

        if (cancelAtPeriodEnd == null) {
            throw new IllegalArgumentException(
                    "Cancel at period end is required."
            );
        }
    }
}
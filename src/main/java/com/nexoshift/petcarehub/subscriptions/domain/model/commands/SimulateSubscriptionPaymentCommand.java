package com.nexoshift.petcarehub.subscriptions.domain.model.commands;

/**
 * Marks the current period of an account's subscription as paid without Stripe.
 * Only for local development, demos and tests.
 *
 * @param accountId the IAM account identifier
 */
public record SimulateSubscriptionPaymentCommand(Long accountId) {

    public SimulateSubscriptionPaymentCommand {
        if (accountId == null || accountId <= 0) {
            throw new IllegalArgumentException(
                    "Account ID must be a positive number."
            );
        }
    }
}

package com.nexoshift.petcarehub.subscriptions.domain.model.commands;

/**
 * Command to create a subscription for an IAM account.
 *
 * @param accountId the IAM account identifier
 * @param planId the selected subscription plan identifier
 */
public record CreateSubscriptionCommand(
        Long accountId,
        Long planId
) {

    public CreateSubscriptionCommand {

        if (accountId == null || accountId <= 0) {
            throw new IllegalArgumentException(
                    "Account ID must be a positive number");
        }

        if (planId == null || planId <= 0) {
            throw new IllegalArgumentException(
                    "Plan ID must be a positive number");
        }
    }
}
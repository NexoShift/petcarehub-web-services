package com.nexoshift.petcarehub.subscriptions.domain.model.queries;

/**
 * Query to retrieve a subscription by its account identifier.
 */
public record GetSubscriptionByAccountIdQuery(Long accountId) {

    public GetSubscriptionByAccountIdQuery {

        if (accountId == null || accountId <= 0) {
            throw new IllegalArgumentException(
                    "Account ID must be a positive number");
        }
    }
}
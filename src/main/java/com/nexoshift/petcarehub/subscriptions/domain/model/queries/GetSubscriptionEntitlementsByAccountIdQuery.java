package com.nexoshift.petcarehub.subscriptions.domain.model.queries;

public record GetSubscriptionEntitlementsByAccountIdQuery(
        Long accountId
) {

    public GetSubscriptionEntitlementsByAccountIdQuery {
        if (accountId == null || accountId <= 0) {
            throw new IllegalArgumentException(
                    "Account ID must be a positive number."
            );
        }
    }
}
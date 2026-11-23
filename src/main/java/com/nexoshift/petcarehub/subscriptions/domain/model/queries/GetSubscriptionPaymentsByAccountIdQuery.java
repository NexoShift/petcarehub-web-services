package com.nexoshift.petcarehub.subscriptions.domain.model.queries;

public record GetSubscriptionPaymentsByAccountIdQuery(
        Long accountId,
        int limit,
        String startingAfter
) {

    public GetSubscriptionPaymentsByAccountIdQuery {

        if (accountId == null || accountId <= 0) {
            throw new IllegalArgumentException(
                    "Account ID must be a positive number."
            );
        }

        if (limit < 1 || limit > 100) {
            throw new IllegalArgumentException(
                    "Limit must be between 1 and 100."
            );
        }

        if (startingAfter != null && startingAfter.isBlank()) {
            startingAfter = null;
        }

        if (startingAfter != null
                && !startingAfter.matches("in_[a-zA-Z0-9]+")) {
            throw new IllegalArgumentException(
                    "Invalid payment history cursor."
            );
        }
    }
}
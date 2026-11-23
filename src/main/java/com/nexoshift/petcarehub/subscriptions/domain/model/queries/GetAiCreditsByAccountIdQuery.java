package com.nexoshift.petcarehub.subscriptions.domain.model.queries;

public record GetAiCreditsByAccountIdQuery(
        Long accountId
) {

    public GetAiCreditsByAccountIdQuery {

        if (accountId == null || accountId <= 0) {
            throw new IllegalArgumentException(
                    "Account ID must be positive");
        }
    }
}
package com.nexoshift.petcarehub.profiles.domain.model.queries;

/**
 * Query to retrieve a clinic by its IAM account identifier.
 *
 * @param accountId the IAM account identifier
 */
public record GetClinicByAccountIdQuery(Long accountId) {

    public GetClinicByAccountIdQuery {
        if (accountId == null || accountId <= 0) {
            throw new IllegalArgumentException(
                    "Account ID must be a positive number");
        }
    }
}

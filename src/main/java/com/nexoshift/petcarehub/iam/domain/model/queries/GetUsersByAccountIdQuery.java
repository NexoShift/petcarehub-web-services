package com.nexoshift.petcarehub.iam.domain.model.queries;

/**
 * Query for retrieving users belonging to an IAM account.
 *
 * @param accountId the account identifier
 */
public record GetUsersByAccountIdQuery(Long accountId) {
}

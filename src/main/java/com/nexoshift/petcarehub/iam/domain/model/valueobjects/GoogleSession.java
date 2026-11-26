package com.nexoshift.petcarehub.iam.domain.model.valueobjects;

/**
 * Result of signing in with Google.
 *
 * @param session the authenticated user and its access token (the same as the normal sign-in)
 * @param accountCreated true when this sign-in created the account
 */
public record GoogleSession(AuthenticatedSession session, boolean accountCreated) {
}

package com.nexoshift.petcarehub.iam.domain.model.valueobjects;

import com.nexoshift.petcarehub.iam.domain.model.entities.User;

/** Result of a successful sign-in: the authenticated user and its access token. */
public record AuthenticatedSession(User user, String token) {
}

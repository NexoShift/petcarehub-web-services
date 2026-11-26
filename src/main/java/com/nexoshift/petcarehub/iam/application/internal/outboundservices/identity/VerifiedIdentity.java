package com.nexoshift.petcarehub.iam.application.internal.outboundservices.identity;

/**
 * An identity confirmed by Google.
 *
 * @param subject Google's id of the person ("sub")
 * @param email the email of the Google account
 * @param emailVerified whether Google verified that email
 */
public record VerifiedIdentity(String subject, String email, boolean emailVerified) {
}

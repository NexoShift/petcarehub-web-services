package com.nexoshift.petcarehub.iam.application.internal.outboundservices.identity;

/**
 * Port to an external identity provider (Google). The adapter checks the token signature,
 * issuer, audience and expiration; IAM only decides what to do with the verified email.
 */
public interface ExternalIdentityVerifier {

  /**
   * @param idToken the ID token the frontend received from Google
   * @return the verified identity
   * @throws com.nexoshift.petcarehub.iam.domain.exceptions.InvalidGoogleTokenException when the
   *         token is not valid (401)
   */
  VerifiedIdentity verifyGoogleIdToken(String idToken);
}

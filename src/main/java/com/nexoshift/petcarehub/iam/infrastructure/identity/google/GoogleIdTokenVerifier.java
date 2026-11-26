package com.nexoshift.petcarehub.iam.infrastructure.identity.google;

import com.nexoshift.petcarehub.iam.application.internal.outboundservices.identity.ExternalIdentityVerifier;
import com.nexoshift.petcarehub.iam.application.internal.outboundservices.identity.VerifiedIdentity;
import com.nexoshift.petcarehub.iam.domain.exceptions.InvalidGoogleTokenException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.JwtTimestampValidator;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 * Checks a Google ID token the way Google documents it:
 * <ol>
 *   <li>The signature, with Google's public keys (downloaded from its JWKS URL and cached).</li>
 *   <li>The issuer is accounts.google.com.</li>
 *   <li>The audience is one of OUR Client IDs (a token made for another app is rejected).</li>
 *   <li>It has not expired.</li>
 * </ol>
 * GOOGLE_CLIENT_ID is required: the API does not start without at least one Client ID.
 */
@Component
public class GoogleIdTokenVerifier implements ExternalIdentityVerifier {

  static final String GOOGLE_JWKS_URI = "https://www.googleapis.com/oauth2/v3/certs";
  static final Set<String> GOOGLE_ISSUERS =
      Set.of("accounts.google.com", "https://accounts.google.com");

  private final List<String> clientIds;
  private final JwtDecoder decoder;

  @Autowired
  public GoogleIdTokenVerifier(@Value("${iam.google.client-ids}") String clientIds) {
    this(parse(clientIds), NimbusJwtDecoder.withJwkSetUri(GOOGLE_JWKS_URI).build());
  }

  /** Tests pass a decoder with their own key instead of Google's. */
  GoogleIdTokenVerifier(List<String> clientIds, JwtDecoder decoder) {
    if (clientIds.isEmpty()) {
      throw new IllegalStateException(
          "Set GOOGLE_CLIENT_ID: the OAuth Client ID(s) of Sign in with Google, separated by commas");
    }
    this.clientIds = clientIds;
    this.decoder = decoder;
    if (decoder instanceof NimbusJwtDecoder nimbus) {
      nimbus.setJwtValidator(validator(clientIds));
    }
  }

  @Override
  public VerifiedIdentity verifyGoogleIdToken(String idToken) {
    Jwt jwt;
    try {
      jwt = decoder.decode(idToken);
    } catch (JwtException exception) {
      throw new InvalidGoogleTokenException("the token could not be verified");
    }
    var email = jwt.getClaimAsString("email");
    if (email == null || email.isBlank()) {
      throw new InvalidGoogleTokenException("the token has no email");
    }
    return new VerifiedIdentity(jwt.getSubject(), email.trim().toLowerCase(),
        Boolean.TRUE.equals(jwt.getClaimAsBoolean("email_verified")));
  }

  static OAuth2TokenValidator<Jwt> validator(List<String> clientIds) {
    OAuth2TokenValidator<Jwt> issuer = jwt -> GOOGLE_ISSUERS.contains(
        String.valueOf(jwt.getClaims().get("iss")))
        ? OAuth2TokenValidatorResult.success()
        : OAuth2TokenValidatorResult.failure(
            new OAuth2Error("invalid_token", "The issuer is not Google", null));
    OAuth2TokenValidator<Jwt> audience = jwt -> jwt.getAudience() != null
        && jwt.getAudience().stream().anyMatch(clientIds::contains)
        ? OAuth2TokenValidatorResult.success()
        : OAuth2TokenValidatorResult.failure(
            new OAuth2Error("invalid_token", "The token was not issued for this app", null));
    return new DelegatingOAuth2TokenValidator<>(new JwtTimestampValidator(), issuer, audience);
  }

  /** "id-web,id-android" -> two Client IDs (web and mobile can use different ones). */
  private static List<String> parse(String clientIds) {
    return Arrays.stream(clientIds == null ? new String[0] : clientIds.split(","))
        .map(String::trim)
        .filter(id -> !id.isEmpty())
        .toList();
  }
}

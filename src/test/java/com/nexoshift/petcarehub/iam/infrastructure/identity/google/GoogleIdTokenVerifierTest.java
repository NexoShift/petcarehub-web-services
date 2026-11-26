package com.nexoshift.petcarehub.iam.infrastructure.identity.google;

import com.nexoshift.petcarehub.iam.domain.exceptions.InvalidGoogleTokenException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * The verifier checks the token as Google says: signature, issuer, audience and expiration.
 * The tokens here are signed with a test key (not Google's), so no network is needed.
 */
class GoogleIdTokenVerifierTest {

  private static final String CLIENT_ID = "123-web.apps.googleusercontent.com";
  private static RSAKey key;
  private static GoogleIdTokenVerifier verifier;

  @BeforeAll
  static void setUp() throws Exception {
    key = new RSAKeyGenerator(2048).keyID("test").generate();
    verifier = new GoogleIdTokenVerifier(List.of(CLIENT_ID, "456-android.apps.googleusercontent.com"),
        NimbusJwtDecoder.withPublicKey(key.toRSAPublicKey()).build());
  }

  private static String token(String issuer, String audience, Instant expiresAt, boolean verified)
      throws Exception {
    var claims = new JWTClaimsSet.Builder()
        .issuer(issuer)
        .audience(audience)
        .subject("google-sub-1")
        .claim("email", "Ana.Rios@gmail.com")
        .claim("email_verified", verified)
        .issueTime(Date.from(expiresAt.minus(1, ChronoUnit.HOURS)))
        .expirationTime(Date.from(expiresAt))
        .build();
    var jwt = new SignedJWT(new JWSHeader.Builder(JWSAlgorithm.RS256).keyID("test").build(), claims);
    jwt.sign(new RSASSASigner(key));
    return jwt.serialize();
  }

  private static Instant inOneHour() {
    return Instant.now().plus(1, ChronoUnit.HOURS);
  }

  @Test
  @DisplayName("A valid token gives the email in lower case and whether Google verified it")
  void validToken() throws Exception {
    var identity = verifier.verifyGoogleIdToken(
        token("https://accounts.google.com", CLIENT_ID, inOneHour(), true));

    assertThat(identity.email()).isEqualTo("ana.rios@gmail.com");
    assertThat(identity.emailVerified()).isTrue();
    assertThat(identity.subject()).isEqualTo("google-sub-1");
  }

  @Test
  @DisplayName("The mobile Client ID is also accepted")
  void secondClientId() throws Exception {
    assertThat(verifier.verifyGoogleIdToken(token("accounts.google.com",
        "456-android.apps.googleusercontent.com", inOneHour(), true)).email())
        .isEqualTo("ana.rios@gmail.com");
  }

  @Test
  @DisplayName("A token made for another app is rejected")
  void otherAudience() {
    assertThatThrownBy(() -> verifier.verifyGoogleIdToken(
        token("https://accounts.google.com", "other-app", inOneHour(), true)))
        .isInstanceOf(InvalidGoogleTokenException.class);
  }

  @Test
  @DisplayName("A token not issued by Google is rejected")
  void otherIssuer() {
    assertThatThrownBy(() -> verifier.verifyGoogleIdToken(
        token("https://evil.example.com", CLIENT_ID, inOneHour(), true)))
        .isInstanceOf(InvalidGoogleTokenException.class);
  }

  @Test
  @DisplayName("An expired token is rejected")
  void expired() {
    assertThatThrownBy(() -> verifier.verifyGoogleIdToken(token("https://accounts.google.com",
        CLIENT_ID, Instant.now().minus(2, ChronoUnit.HOURS), true)))
        .isInstanceOf(InvalidGoogleTokenException.class);
  }

  @Test
  @DisplayName("A token signed with another key is rejected")
  void otherSignature() throws Exception {
    var otherKey = new RSAKeyGenerator(2048).generate();
    var otherVerifier = new GoogleIdTokenVerifier(List.of(CLIENT_ID),
        NimbusJwtDecoder.withPublicKey(otherKey.toRSAPublicKey()).build());

    assertThatThrownBy(() -> otherVerifier.verifyGoogleIdToken(
        token("https://accounts.google.com", CLIENT_ID, inOneHour(), true)))
        .isInstanceOf(InvalidGoogleTokenException.class);
  }

  @Test
  @DisplayName("Without GOOGLE_CLIENT_ID the API does not start")
  void notConfigured() {
    assertThatThrownBy(() -> new GoogleIdTokenVerifier(" , "))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("GOOGLE_CLIENT_ID");
  }
}

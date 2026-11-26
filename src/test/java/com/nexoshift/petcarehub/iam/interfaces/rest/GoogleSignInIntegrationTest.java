package com.nexoshift.petcarehub.iam.interfaces.rest;

import com.nexoshift.petcarehub.iam.application.internal.outboundservices.identity.ExternalIdentityVerifier;
import com.nexoshift.petcarehub.iam.application.internal.outboundservices.identity.VerifiedIdentity;
import com.nexoshift.petcarehub.iam.domain.exceptions.InvalidGoogleTokenException;
import com.nexoshift.petcarehub.support.PetCareApiClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpMethod;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Login con Google de punta a punta, con un verificador falso en lugar de Google:
 * "google:correo" es un token válido y "unverified:correo" uno con el correo sin verificar.
 */
@SpringBootTest(properties = "spring.profiles.active=test")
class GoogleSignInIntegrationTest {

  @TestConfiguration
  static class FakeGoogle {
    @Bean
    @Primary
    ExternalIdentityVerifier fakeGoogleVerifier() {
      return idToken -> {
        var parts = idToken.split(":", 2);
        if (parts.length != 2) {
          throw new InvalidGoogleTokenException("the token could not be verified");
        }
        return new VerifiedIdentity("sub-" + parts[1], parts[1], parts[0].equals("google"));
      };
    }
  }

  private static final String URL = "/api/v1/authentication/google-sign-in";

  @Autowired
  private WebApplicationContext context;

  private PetCareApiClient api;
  private String email;

  @BeforeEach
  void setUp() {
    api = new PetCareApiClient(MockMvcBuilders.webAppContextSetup(context)
        .apply(SecurityMockMvcConfigurers.springSecurity()).build());
    email = "google-" + UUID.randomUUID() + "@gmail.com";
  }

  private PetCareApiClient.Reply signIn(String idToken, String accountType) throws Exception {
    var type = accountType == null ? "" : ", \"accountType\": \"" + accountType + "\"";
    return api.send(HttpMethod.POST, URL, null, "{\"idToken\": \"" + idToken + "\"" + type + "}");
  }

  @Test
  @DisplayName("First time without accountType: 422 so the frontend asks owner or clinic")
  void firstTimeNeedsAccountType() throws Exception {
    var reply = signIn("google:" + email, null);

    assertThat(reply.status()).isEqualTo(422);
    assertThat(reply.body()).contains("ACCOUNT_TYPE_REQUIRED");
  }

  @Test
  @DisplayName("First time with accountType creates the account (201); the next time it signs in (200)")
  void createsThenSignsIn() throws Exception {
    var created = signIn("google:" + email, "PET_OWNER");
    assertThat(created.status()).as(created.body()).isEqualTo(201);
    assertThat(created.body()).contains("\"accountType\":\"PET_OWNER\"", "ROLE_PET_OWNER");

    var me = api.send(HttpMethod.GET, "/api/v1/users/me", created.text("token"), null);
    assertThat(me.status()).isEqualTo(200);
    assertThat(me.body()).contains(email);

    var again = signIn("google:" + email, null);
    assertThat(again.status()).isEqualTo(200);
    assertThat(again.number("userId")).isEqualTo(created.number("userId"));
  }

  @Test
  @DisplayName("Someone who registered with a password can also enter with Google (same user)")
  void existingPasswordAccount() throws Exception {
    var session = api.signUpAndSignIn("CLINIC");
    var me = api.send(HttpMethod.GET, "/api/v1/users/me", session.token(), null);
    var existingEmail = PetCareApiClient.extract(me.body(), "\"email\":\"([^\"]+)\"");

    var reply = signIn("google:" + existingEmail, null);

    assertThat(reply.status()).as(reply.body()).isEqualTo(200);
    assertThat(reply.number("userId")).isEqualTo(session.userId());
  }

  @Test
  @DisplayName("An invalid token or an email not verified by Google is 401")
  void rejected() throws Exception {
    assertThat(signIn("not-a-google-token", "PET_OWNER").status()).isEqualTo(401);
    assertThat(signIn("unverified:" + email, "PET_OWNER").status()).isEqualTo(401);
    assertThat(signIn("google:" + email, "ADMIN").status()).isEqualTo(400);
  }
}

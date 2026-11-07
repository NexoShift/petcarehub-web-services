package com.nexoshift.petcarehub.iam.interfaces.rest;

import com.nexoshift.petcarehub.iam.support.IamApiClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/** Onboarding step 1: IAM only handles e-mail, password and account type. */
@SpringBootTest(properties = "spring.profiles.active=test")
class AuthenticationControllerIntegrationTest {

  @Autowired
  private WebApplicationContext context;

  private IamApiClient api;

  @BeforeEach
  void setUp() {
    api = new IamApiClient(MockMvcBuilders.webAppContextSetup(context)
        .apply(SecurityMockMvcConfigurers.springSecurity())
        .build());
  }

  private static String email(String prefix) {
    return prefix + "-" + UUID.randomUUID() + "@petcare.test";
  }

  @Test
  @DisplayName("Sign-up de dueño solo con email, contraseña y tipo de cuenta")
  void petOwnerSignsUpWithCredentialsOnly() throws Exception {
    var email = email("owner");

    var reply = api.signUp(email, IamApiClient.PASSWORD, "PET_OWNER");

    assertThat(reply.status()).isEqualTo(201);
    assertThat(reply.text("email")).isEqualTo(email);
    assertThat(reply.text("accountType")).isEqualTo("PET_OWNER");
    assertThat(reply.text("accessRole")).isEqualTo("HOLDER");
    assertThat(reply.body()).contains("ROLE_PET_OWNER").doesNotContain("firstName", "phone");
    assertThat(reply.flag("mustChangePassword")).isFalse();
  }

  @Test
  @DisplayName("Sign-up de clínica: el titular recibe CLINIC_ADMIN y VETERINARIAN")
  void clinicHolderGetsAdminAndVeterinarianRoles() throws Exception {
    var reply = api.signUp(email("clinic"), IamApiClient.PASSWORD, "CLINIC");

    assertThat(reply.status()).isEqualTo(201);
    assertThat(reply.body()).contains("ROLE_CLINIC_ADMIN", "ROLE_VETERINARIAN");
  }

  @Test
  @DisplayName("El sign-in devuelve el token, los ids para el paso 2 y mustChangePassword=false")
  void signInReturnsWhatTheOnboardingNeeds() throws Exception {
    var email = email("owner");
    var userId = api.signUp(email, IamApiClient.PASSWORD, "PET_OWNER").number("id");

    var reply = api.signIn(email, IamApiClient.PASSWORD);

    assertThat(reply.status()).isEqualTo(200);
    assertThat(reply.number("userId")).isEqualTo(userId);
    assertThat(reply.number("accountId")).isPositive();
    assertThat(reply.text("token")).isNotBlank();
    assertThat(reply.flag("mustChangePassword")).isFalse();

    var me = api.send(HttpMethod.GET, "/api/v1/users/me", reply.text("token"), null);
    assertThat(me.status()).isEqualTo(200);
    assertThat(me.number("id")).isEqualTo(userId);
  }

  @Test
  @DisplayName("Un email repetido da 409")
  void duplicatedEmailIsRejected() throws Exception {
    var email = email("dup");
    assertThat(api.signUp(email, IamApiClient.PASSWORD, "PET_OWNER").status()).isEqualTo(201);

    assertThat(api.signUp(email, IamApiClient.PASSWORD, "CLINIC").status()).isEqualTo(409);
  }

  @Test
  @DisplayName("Datos inválidos dan 400 y credenciales incorrectas 401")
  void invalidRequestsAreRejected() throws Exception {
    assertThat(api.signUp("no-es-email", IamApiClient.PASSWORD, "PET_OWNER").status())
        .isEqualTo(400);
    assertThat(api.signUp(email("short"), "123", "PET_OWNER").status()).isEqualTo(400);
    assertThat(api.send(HttpMethod.POST, "/api/v1/authentication/sign-up", null,
        "{\"email\":\"%s\",\"password\":\"%s\"}".formatted(email("x"), IamApiClient.PASSWORD))
        .status()).isEqualTo(400);
    assertThat(api.signIn(email("nobody"), IamApiClient.PASSWORD).status()).isEqualTo(401);
    assertThat(api.send(HttpMethod.GET, "/api/v1/users/me", null, null).status()).isEqualTo(401);
  }
}

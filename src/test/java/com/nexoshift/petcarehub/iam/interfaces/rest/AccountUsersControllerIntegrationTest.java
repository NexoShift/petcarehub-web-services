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

/**
 * The clinic admin creates veterinarians with a temporary password
 * (POST /accounts/{accountId}/users). The veterinarian must change it after the first sign-in.
 */
@SpringBootTest(properties = "spring.profiles.active=test")
class AccountUsersControllerIntegrationTest {

  private static final String TEMPORARY = "Temporal2026";

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

  private static String usersOf(long accountId) {
    return "/api/v1/accounts/" + accountId + "/users";
  }

  private static String body(String email, String password) {
    return "{\"email\":\"%s\",\"password\":\"%s\"}".formatted(email, password);
  }

  private IamApiClient.Reply createMember(IamApiClient.Reply admin, String email) throws Exception {
    return api.send(HttpMethod.POST, usersOf(admin.number("accountId")), admin.text("token"),
        body(email, TEMPORARY));
  }

  @Test
  @DisplayName("Flujo completo: crear vet, primer login con mustChangePassword, cambiarla y entrar")
  void memberMustReplaceTheTemporaryPassword() throws Exception {
    var admin = api.registerAndSignIn(email("admin"), "CLINIC");
    var vetEmail = email("vet");

    var member = createMember(admin, vetEmail);

    assertThat(member.status()).isEqualTo(201);
    assertThat(member.text("accessRole")).isEqualTo("MEMBER");
    assertThat(member.number("accountId")).isEqualTo(admin.number("accountId"));
    assertThat(member.flag("mustChangePassword")).isTrue();

    var firstSignIn = api.signIn(vetEmail, TEMPORARY);
    assertThat(firstSignIn.status()).isEqualTo(200);
    assertThat(firstSignIn.flag("mustChangePassword")).isTrue();
    assertThat(firstSignIn.body()).contains("ROLE_VETERINARIAN").doesNotContain("ROLE_CLINIC_ADMIN");

    var token = firstSignIn.text("token");
    var me = api.send(HttpMethod.GET, "/api/v1/users/me", token, null);
    assertThat(me.flag("mustChangePassword")).isTrue();

    var change = api.send(HttpMethod.PUT, "/api/v1/users/" + firstSignIn.number("userId")
        + "/password", token,
        "{\"currentPassword\":\"%s\",\"newPassword\":\"NuevaClave2026\"}".formatted(TEMPORARY));
    assertThat(change.status()).isEqualTo(200);

    assertThat(api.signIn(vetEmail, TEMPORARY).status()).isEqualTo(401);
    var secondSignIn = api.signIn(vetEmail, "NuevaClave2026");
    assertThat(secondSignIn.status()).isEqualTo(200);
    assertThat(secondSignIn.flag("mustChangePassword")).isFalse();
  }

  @Test
  @DisplayName("El titular de la clínica nunca tiene que cambiar su contraseña")
  void holderDoesNotHaveATemporaryPassword() throws Exception {
    var admin = api.registerAndSignIn(email("admin"), "CLINIC");

    assertThat(admin.flag("mustChangePassword")).isFalse();
  }

  @Test
  @DisplayName("Un dueño de mascota no puede crear veterinarios (403)")
  void petOwnerCannotCreateMembers() throws Exception {
    var owner = api.registerAndSignIn(email("owner"), "PET_OWNER");

    var reply = api.send(HttpMethod.POST, usersOf(owner.number("accountId")),
        owner.text("token"), body(email("vet"), TEMPORARY));

    assertThat(reply.status()).isEqualTo(403);
  }

  @Test
  @DisplayName("Un admin no puede crear veterinarios en otra clínica (403)")
  void adminCannotCreateMembersInAnotherClinic() throws Exception {
    var admin = api.registerAndSignIn(email("admin"), "CLINIC");
    var otherClinic = api.registerAndSignIn(email("other"), "CLINIC");

    var reply = api.send(HttpMethod.POST, usersOf(otherClinic.number("accountId")),
        admin.text("token"), body(email("vet"), TEMPORARY));

    assertThat(reply.status()).isEqualTo(403);
  }

  @Test
  @DisplayName("Un veterinario miembro no puede crear otros veterinarios (403)")
  void memberCannotCreateMembers() throws Exception {
    var admin = api.registerAndSignIn(email("admin"), "CLINIC");
    var vetEmail = email("vet");
    createMember(admin, vetEmail);
    var vet = api.signIn(vetEmail, TEMPORARY);

    var reply = api.send(HttpMethod.POST, usersOf(vet.number("accountId")), vet.text("token"),
        body(email("x"), TEMPORARY));

    assertThat(reply.status()).isEqualTo(403);
  }

  @Test
  @DisplayName("Sin token da 401, datos inválidos 400 y email repetido 409")
  void invalidRequestsAreRejected() throws Exception {
    var admin = api.registerAndSignIn(email("admin"), "CLINIC");
    var url = usersOf(admin.number("accountId"));
    var token = admin.text("token");
    var vetEmail = email("vet");

    assertThat(api.send(HttpMethod.POST, url, null, body(vetEmail, TEMPORARY)).status())
        .isEqualTo(401);
    assertThat(api.send(HttpMethod.POST, url, token, body("no-es-email", TEMPORARY)).status())
        .isEqualTo(400);
    assertThat(api.send(HttpMethod.POST, url, token, body(vetEmail, "123")).status())
        .isEqualTo(400);
    assertThat(api.send(HttpMethod.POST, url, token, body(vetEmail, TEMPORARY)).status())
        .isEqualTo(201);
    assertThat(api.send(HttpMethod.POST, url, token, body(vetEmail, TEMPORARY)).status())
        .isEqualTo(409);
  }
}

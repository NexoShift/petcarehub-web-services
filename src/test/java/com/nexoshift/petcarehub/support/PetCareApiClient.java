package com.nexoshift.petcarehub.support;

import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Cliente de la API para los tests de integración que cruzan BCs (Clinical Attention, AI).
 * <p>
 * Crea datos REALES a través de los endpoints de IAM y Profiles, igual que lo haría el
 * frontend: así los tests prueban la comunicación entre BCs y no un adaptador falso.
 * Cada llamada usa emails y RUC aleatorios para que los tests no choquen entre sí.
 */
public final class PetCareApiClient {

  public static final String PASSWORD = "Password123!";

  private final MockMvc mockMvc;

  public PetCareApiClient(MockMvc mockMvc) {
    this.mockMvc = mockMvc;
  }

  /** Respuesta HTTP simplificada. */
  public record Reply(int status, String body, String location) {

    /** Primer número del campo, p. ej. number("id"). */
    public long number(String field) {
      return Long.parseLong(extract(body, "\"" + field + "\":(\\d+)"));
    }

    public String text(String field) {
      return extract(body, "\"" + field + "\":\"([^\"]+)\"");
    }
  }

  /** Usuario de IAM ya autenticado. */
  public record Session(String token, long userId, long accountId) {
  }

  /** Clínica con su titular (que también es veterinario), un consultorio activo y su vet. */
  public record ClinicSetup(Session holder, long clinicId, long veterinarianId,
                            long consultingRoomId) {
  }

  /** Veterinario miembro de una clínica (no es el titular). */
  public record MemberSetup(Session member, long veterinarianId) {
  }

  /** Dueño con una mascota. */
  public record OwnerSetup(Session owner, long petOwnerId, long petId) {
  }

  // ---------------------------------------------------------------- HTTP

  public Reply send(HttpMethod method, String url, String token, String json) throws Exception {
    var request = MockMvcRequestBuilders.request(method, url).accept(MediaType.APPLICATION_JSON);
    if (token != null) {
      request = request.header("Authorization", "Bearer " + token);
    }
    if (json != null) {
      request = request.contentType(MediaType.APPLICATION_JSON).content(json);
    }
    var response = mockMvc.perform(request).andReturn().getResponse();
    return new Reply(response.getStatus(), response.getContentAsString(),
        response.getHeader("Location"));
  }

  /** POST multipart/form-data con campos de texto (los create de Profiles usan @ModelAttribute). */
  public Reply sendForm(String url, String token, Map<String, ?> fields) throws Exception {
    var request = MockMvcRequestBuilders.multipart(url).accept(MediaType.APPLICATION_JSON);
    fields.forEach((name, value) -> request.param(name, String.valueOf(value)));
    if (token != null) {
      request.header("Authorization", "Bearer " + token);
    }
    var response = mockMvc.perform(request).andReturn().getResponse();
    return new Reply(response.getStatus(), response.getContentAsString(),
        response.getHeader("Location"));
  }

  /** POST multipart con un archivo (parte "file") y parámetros. */
  public Reply sendFile(String url, String token, MockMultipartFile file,
                        Map<String, ?> params) throws Exception {
    var request = MockMvcRequestBuilders.multipart(url).accept(MediaType.APPLICATION_JSON);
    if (file != null) {
      request.file(file);
    }
    params.forEach((name, value) -> request.param(name, String.valueOf(value)));
    request.header("Authorization", "Bearer " + token);
    var response = mockMvc.perform(request).andReturn().getResponse();
    return new Reply(response.getStatus(), response.getContentAsString(),
        response.getHeader("Location"));
  }

  // ---------------------------------------------------------------- IAM

  /** sign-up + sign-in. accountType: PET_OWNER o CLINIC. */
  public Session signUpAndSignIn(String accountType) throws Exception {
    var email = accountType.toLowerCase() + "-" + UUID.randomUUID() + "@petcare.test";
    var signUp = send(HttpMethod.POST, "/api/v1/authentication/sign-up", null,
        "{\"email\": \"%s\", \"password\": \"%s\", \"accountType\": \"%s\"}"
            .formatted(email, PASSWORD, accountType));
    assertThat(signUp.status()).as("sign-up " + signUp.body()).isEqualTo(201);
    var signIn = send(HttpMethod.POST, "/api/v1/authentication/sign-in", null,
        "{\"email\": \"%s\", \"password\": \"%s\"}".formatted(email, PASSWORD));
    assertThat(signIn.status()).as("sign-in " + signIn.body()).isEqualTo(200);
    return new Session(signIn.text("token"), signIn.number("userId"),
        signIn.number("accountId"));
  }

  // ---------------------------------------------------------------- Profiles

  /** Cuenta CLINIC + perfil de clínica + perfil vet del titular + consultorio activo. */
  public ClinicSetup clinicWithVeterinarian() throws Exception {
    var holder = signUpAndSignIn("CLINIC");
    var clinic = sendForm("/api/v1/clinics", holder.token(), Map.of(
        "accountId", holder.accountId(),
        "name", "Clínica " + holder.userId(),
        "ruc", "20" + ThreadLocalRandom.current().nextLong(100_000_000L, 999_999_999L),
        "phoneNumber", "987654321",
        "address", "Av. Los Olivos 1234"));
    assertThat(clinic.status()).as("clinic " + clinic.body()).isEqualTo(201);
    var clinicId = clinic.number("id");

    var vet = sendForm("/api/v1/veterinarians", holder.token(), Map.of(
        "userId", holder.userId(),
        "clinicId", clinicId,
        "firstName", "Ana",
        "lastName", "López",
        "email", "vet" + holder.userId() + "@petcare.test",
        "licenseNumber", "CMVP " + holder.userId()));
    assertThat(vet.status()).as("veterinarian " + vet.body()).isEqualTo(201);

    var room = send(HttpMethod.POST, "/api/v1/clinics/" + clinicId + "/consulting-rooms",
        holder.token(), "{\"name\": \"Consultorio 1\", \"description\": \"General\"}");
    assertThat(room.status()).as("consulting room " + room.body()).isEqualTo(201);

    return new ClinicSetup(holder, clinicId, vet.number("id"), room.number("id"));
  }

  /**
   * El titular da de alta a otro veterinario en su cuenta (IAM) y le crea el perfil (Profiles).
   * Sirve para probar "otro vet de la MISMA clínica".
   */
  public MemberSetup addVeterinarian(ClinicSetup clinic) throws Exception {
    var email = "member-" + UUID.randomUUID() + "@petcare.test";
    var member = send(HttpMethod.POST,
        "/api/v1/accounts/" + clinic.holder().accountId() + "/users", clinic.holder().token(),
        "{\"email\": \"%s\", \"password\": \"%s\"}".formatted(email, PASSWORD));
    assertThat(member.status()).as("member " + member.body()).isEqualTo(201);
    var signIn = send(HttpMethod.POST, "/api/v1/authentication/sign-in", null,
        "{\"email\": \"%s\", \"password\": \"%s\"}".formatted(email, PASSWORD));
    assertThat(signIn.status()).as("member sign-in " + signIn.body()).isEqualTo(200);
    var session = new Session(signIn.text("token"), signIn.number("userId"),
        signIn.number("accountId"));

    var vet = sendForm("/api/v1/veterinarians", clinic.holder().token(), Map.of(
        "userId", session.userId(),
        "clinicId", clinic.clinicId(),
        "firstName", "Luis",
        "lastName", "Paz",
        "email", email,
        "licenseNumber", "CMVP " + session.userId()));
    assertThat(vet.status()).as("member veterinarian " + vet.body()).isEqualTo(201);
    return new MemberSetup(session, vet.number("id"));
  }

  /** Cuenta PET_OWNER + perfil de dueño + una mascota. */
  public OwnerSetup ownerWithPet() throws Exception {
    var owner = signUpAndSignIn("PET_OWNER");
    var petOwner = sendForm("/api/v1/pet-owners", owner.token(), Map.of(
        "userId", owner.userId(),
        "firstName", "Ares",
        "lastName", "Torres",
        "email", "owner" + owner.userId() + "@petcare.test",
        "phoneNumber", "912345678"));
    assertThat(petOwner.status()).as("pet owner " + petOwner.body()).isEqualTo(201);
    var petOwnerId = petOwner.number("id");

    var pet = sendForm("/api/v1/pets", owner.token(), Map.of(
        "ownerId", petOwnerId,
        "name", "Spot",
        "species", "DOG",
        "breed", "Mestizo",
        "birthDate", "2023-09-15",
        "sex", "MALE"));
    assertThat(pet.status()).as("pet " + pet.body()).isEqualTo(201);
    return new OwnerSetup(owner, petOwnerId, pet.number("id"));
  }

  /** La clínica registra la mascota como paciente. */
  public void registerPatient(ClinicSetup clinic, long petId) throws Exception {
    var reply = send(HttpMethod.POST, "/api/v1/clinics/" + clinic.clinicId() + "/patients",
        clinic.holder().token(), "{\"petId\": " + petId + "}");
    assertThat(reply.status()).as("patient " + reply.body()).isBetween(200, 201);
  }

  // ---------------------------------------------------------------- util

  public static String extract(String json, String regex) {
    var matcher = Pattern.compile(regex).matcher(json);
    assertThat(matcher.find()).as("'" + regex + "' in " + json).isTrue();
    return matcher.group(1);
  }
}

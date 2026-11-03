package com.nexoshift.petcarehub.clinicalattention.interfaces.rest;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;
import java.util.regex.Pattern;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

/**
 * End-to-end test of the REST API with the real security chain, JPA mappings and an H2 database
 * (profile "test"). Users are created through the authentication endpoints. A CLINIC account has
 * the VETERINARIAN role; a PET_OWNER account does not.
 */
@SpringBootTest(properties = "spring.profiles.active=test")
class ClinicalConsultationsControllerIntegrationTest {

  private static final String BASE = "/api/v1/clinical-consultations";
  private static final String PASSWORD = "Password123!";

  private static final String CREATE_BODY = """
      {"petId": 5, "clinicId": 3, "consultingRoomId": 7, "reason": "Vomits since yesterday"}""";
  private static final String ANAMNESIS_BODY = """
      {"chiefComplaint": "Vomiting", "evolutionTime": "2 days",
       "symptoms": ["Vomiting", "Lethargy"], "diet": "Dry food"}""";
  private static final String ASSESSMENT_BODY = """
      {"weightKg": 12.5, "temperatureCelsius": 38.6, "heartRateBpm": 110,
       "respiratoryRateBpm": 28, "bodyConditionScore": 5, "hydrationStatus": "MILD_DEHYDRATION"}""";
  private static final String FINDING_BODY = """
      {"bodySystem": "DIGESTIVE", "description": "Painful abdomen", "severity": "MODERATE"}""";
  private static final String PRIMARY_DIAGNOSIS_BODY = """
      {"description": "Acute gastroenteritis", "type": "PRESUMPTIVE", "primary": true,
       "differentialDiagnoses": ["Pancreatitis"], "rationale": "Vomiting after a food change"}""";
  private static final String REEVALUATION_BODY = """
      {"description": "Pancreatitis", "type": "DEFINITIVE", "differentialDiagnoses": [],
       "rationale": "Lipase elevated"}""";

  @Autowired
  private WebApplicationContext context;

  private MockMvc mockMvc;
  private String veterinarianToken;

  private record Reply(int status, String body, String location) {
  }

  @BeforeEach
  void setUp() throws Exception {
    mockMvc = MockMvcBuilders.webAppContextSetup(context)
        .apply(SecurityMockMvcConfigurers.springSecurity())
        .build();
    veterinarianToken = signUpAndSignIn("CLINIC");
  }

  // ------------------------------------------------------------------ helpers

  private Reply send(HttpMethod method, String url, String token, String json) throws Exception {
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

  private String signUpAndSignIn(String accountType) throws Exception {
    var email = "vet-" + UUID.randomUUID() + "@petcare.test";
    send(HttpMethod.POST, "/api/v1/authentication/sign-up", null,
        "{\"email\": \"%s\", \"password\": \"%s\", \"accountType\": \"%s\"}"
            .formatted(email, PASSWORD, accountType));
    var signIn = send(HttpMethod.POST, "/api/v1/authentication/sign-in", null,
        "{\"email\": \"%s\", \"password\": \"%s\"}".formatted(email, PASSWORD));
    assertThat(signIn.status()).isEqualTo(200);
    return extract(signIn.body(), "\"token\":\"([^\"]+)\"");
  }

  private static String extract(String json, String regex) {
    var matcher = Pattern.compile(regex).matcher(json);
    assertThat(matcher.find()).isTrue();
    return matcher.group(1);
  }

  private long createConsultation(String token) throws Exception {
    var reply = send(HttpMethod.POST, BASE, token, CREATE_BODY);
    assertThat(reply.status()).isEqualTo(201);
    return Long.parseLong(extract(reply.body(), "\"id\":(\\d+)"));
  }

  private long startedConsultation() throws Exception {
    var id = createConsultation(veterinarianToken);
    assertThat(send(HttpMethod.PUT, BASE + "/" + id + "/start", veterinarianToken, null).status())
        .isEqualTo(200);
    return id;
  }

  // ----------------------------------------------------------------- security

  @Test
  @DisplayName("Without a token the API answers 401")
  void requiresAuthentication() throws Exception {
    assertThat(send(HttpMethod.POST, BASE, null, CREATE_BODY).status()).isEqualTo(401);
    assertThat(send(HttpMethod.GET, BASE + "/1", null, null).status()).isEqualTo(401);
  }

  @Test
  @DisplayName("A pet owner cannot use the veterinarian endpoints")
  void petOwnerIsForbidden() throws Exception {
    var ownerToken = signUpAndSignIn("PET_OWNER");

    assertThat(send(HttpMethod.POST, BASE, ownerToken, CREATE_BODY).status()).isEqualTo(403);
    assertThat(send(HttpMethod.GET, BASE + "/1", ownerToken, null).status()).isEqualTo(403);
  }

  @Test
  @DisplayName("Only the veterinarian in charge can change a consultation")
  void otherVeterinarianIsForbidden() throws Exception {
    var id = startedConsultation();
    var otherVeterinarianToken = signUpAndSignIn("CLINIC");

    assertThat(send(HttpMethod.PUT, BASE + "/" + id + "/cancel", otherVeterinarianToken,
        "{\"reason\": \"Not mine\"}").status()).isEqualTo(403);
    assertThat(send(HttpMethod.POST, BASE + "/" + id + "/anamnesis", otherVeterinarianToken,
        ANAMNESIS_BODY).status()).isEqualTo(403);
    assertThat(send(HttpMethod.GET, BASE + "/" + id, otherVeterinarianToken, null).status())
        .isEqualTo(200);
  }

  // ------------------------------------------------------------------- create

  @Test
  @DisplayName("POST creates a CREATED consultation and returns its location")
  void createsConsultation() throws Exception {
    var reply = send(HttpMethod.POST, BASE, veterinarianToken, CREATE_BODY);

    assertThat(reply.status()).isEqualTo(201);
    var id = extract(reply.body(), "\"id\":(\\d+)");
    assertThat(reply.location()).endsWith(BASE + "/" + id);
    assertThat(reply.body()).contains("\"status\":\"CREATED\"");
    assertThat(reply.body()).contains("\"petId\":5");
    assertThat(reply.body()).contains("\"reason\":\"Vomits since yesterday\"");
  }

  @Test
  @DisplayName("Invalid data is answered with 400 and the invalid fields")
  void rejectsInvalidData() throws Exception {
    var blankReason = send(HttpMethod.POST, BASE, veterinarianToken,
        "{\"petId\": 5, \"clinicId\": 3, \"reason\": \" \"}");
    var missingPet = send(HttpMethod.POST, BASE, veterinarianToken,
        "{\"clinicId\": 3, \"reason\": \"Check-up\"}");
    var malformed = send(HttpMethod.POST, BASE, veterinarianToken, "{\"petId\": ");

    assertThat(blankReason.status()).isEqualTo(400);
    assertThat(blankReason.body()).contains("reason");
    assertThat(missingPet.status()).isEqualTo(400);
    assertThat(missingPet.body()).contains("petId");
    assertThat(malformed.status()).isEqualTo(400);
  }

  @Test
  @DisplayName("GET returns the consultation and 404 when it does not exist")
  void getsConsultation() throws Exception {
    var id = createConsultation(veterinarianToken);

    var found = send(HttpMethod.GET, BASE + "/" + id, veterinarianToken, null);
    var missing = send(HttpMethod.GET, BASE + "/999999", veterinarianToken, null);

    assertThat(found.status()).isEqualTo(200);
    assertThat(found.body()).contains("\"id\":" + id);
    assertThat(missing.status()).isEqualTo(404);
  }

  // -------------------------------------------------------------------- flow

  @Test
  @DisplayName("Records need the consultation to be started")
  void recordsNeedStartedConsultation() throws Exception {
    var id = createConsultation(veterinarianToken);

    var reply = send(HttpMethod.POST, BASE + "/" + id + "/anamnesis", veterinarianToken,
        ANAMNESIS_BODY);

    assertThat(reply.status()).isEqualTo(409);
  }

  @Test
  @DisplayName("Complete flow: records, reevaluation without overwriting, completion and closing")
  void completeFlow() throws Exception {
    var id = startedConsultation();
    var url = BASE + "/" + id;

    var anamnesis = send(HttpMethod.POST, url + "/anamnesis", veterinarianToken, ANAMNESIS_BODY);
    assertThat(anamnesis.status()).isEqualTo(201);
    assertThat(anamnesis.body()).contains("\"chiefComplaint\":\"Vomiting\"");
    assertThat(anamnesis.body()).contains("\"symptoms\":[\"Vomiting\",\"Lethargy\"]");

    assertThat(send(HttpMethod.POST, url + "/anamnesis", veterinarianToken, ANAMNESIS_BODY)
        .status()).isEqualTo(409);

    assertThat(send(HttpMethod.POST, url + "/physiological-assessments", veterinarianToken,
        ASSESSMENT_BODY).status()).isEqualTo(201);
    var secondAssessment = send(HttpMethod.POST, url + "/physiological-assessments",
        veterinarianToken, ASSESSMENT_BODY);
    assertThat(secondAssessment.status()).isEqualTo(201);
    assertThat(secondAssessment.body().split("\"weightKg\"", -1).length - 1).isEqualTo(2);

    var finding = send(HttpMethod.POST, url + "/findings", veterinarianToken, FINDING_BODY);
    assertThat(finding.status()).isEqualTo(201);
    assertThat(finding.body()).contains("\"bodySystem\":\"DIGESTIVE\"");

    var diagnosis = send(HttpMethod.POST, url + "/diagnoses", veterinarianToken,
        PRIMARY_DIAGNOSIS_BODY);
    assertThat(diagnosis.status()).isEqualTo(201);
    var diagnosisId = extract(diagnosis.body(), "\"diagnoses\":\\[\\{\"id\":(\\d+)");

    // a second primary diagnosis is not allowed: it must be a reevaluation
    assertThat(send(HttpMethod.POST, url + "/diagnoses", veterinarianToken, PRIMARY_DIAGNOSIS_BODY)
        .status()).isEqualTo(409);

    var reevaluation = send(HttpMethod.POST, url + "/diagnoses/" + diagnosisId + "/reevaluations",
        veterinarianToken, REEVALUATION_BODY);
    assertThat(reevaluation.status()).isEqualTo(201);
    assertThat(reevaluation.body()).contains("\"status\":\"SUPERSEDED\"");
    assertThat(reevaluation.body()).contains("\"description\":\"Acute gastroenteritis\"");
    assertThat(reevaluation.body()).contains("\"description\":\"Pancreatitis\"");
    assertThat(reevaluation.body()).contains("\"previousDiagnosisId\":" + diagnosisId);

    var completed = send(HttpMethod.PUT, url + "/complete", veterinarianToken, null);
    assertThat(completed.status()).isEqualTo(200);
    assertThat(completed.body()).contains("\"status\":\"COMPLETED\"");

    // the consultation is closed
    assertThat(send(HttpMethod.POST, url + "/findings", veterinarianToken, FINDING_BODY).status())
        .isEqualTo(409);
    assertThat(send(HttpMethod.PUT, url + "/cancel", veterinarianToken, "{\"reason\": \"Mistake\"}")
        .status()).isEqualTo(409);

    var stored = send(HttpMethod.GET, url, veterinarianToken, null);
    assertThat(stored.body()).contains("\"status\":\"COMPLETED\"");
    assertThat(stored.body()).contains("\"previousDiagnosisId\":" + diagnosisId);
  }

  @Test
  @DisplayName("Completing without the required data answers 409 with the pending items")
  void incompleteCompletion() throws Exception {
    var id = startedConsultation();

    var reply = send(HttpMethod.PUT, BASE + "/" + id + "/complete", veterinarianToken, null);

    assertThat(reply.status()).isEqualTo(409);
    assertThat(reply.body()).contains("anamnesis");
    assertThat(reply.body()).contains("physiologicalAssessment");
    assertThat(reply.body()).contains("primaryDiagnosis");
    var stored = send(HttpMethod.GET, BASE + "/" + id, veterinarianToken, null);
    assertThat(stored.body()).contains("\"status\":\"IN_PROGRESS\"");
  }

  @Test
  @DisplayName("Invalid clinical data is answered with 400")
  void rejectsInvalidClinicalData() throws Exception {
    var id = startedConsultation();
    var url = BASE + "/" + id;

    var coldPet = send(HttpMethod.POST, url + "/physiological-assessments", veterinarianToken,
        ASSESSMENT_BODY.replace("38.6", "20"));
    var unknownBodySystem = send(HttpMethod.POST, url + "/findings", veterinarianToken,
        FINDING_BODY.replace("DIGESTIVE", "TAIL"));
    var withoutType = send(HttpMethod.POST, url + "/diagnoses", veterinarianToken,
        "{\"description\": \"Gastritis\", \"primary\": true}");
    var unknownDiagnosis = send(HttpMethod.POST, url + "/diagnoses/999999/reevaluations",
        veterinarianToken, REEVALUATION_BODY);

    assertThat(coldPet.status()).isEqualTo(400);
    assertThat(unknownBodySystem.status()).isEqualTo(400);
    assertThat(withoutType.status()).isEqualTo(400);
    assertThat(unknownDiagnosis.status()).isEqualTo(400);
  }

  @Test
  @DisplayName("Cancelling needs a reason and keeps the consultation as CANCELLED")
  void cancelsConsultation() throws Exception {
    var id = createConsultation(veterinarianToken);
    var url = BASE + "/" + id + "/cancel";

    assertThat(send(HttpMethod.PUT, url, veterinarianToken, "{\"reason\": \"\"}").status())
        .isEqualTo(400);
    var cancelled = send(HttpMethod.PUT, url, veterinarianToken, "{\"reason\": \"No show\"}");

    assertThat(cancelled.status()).isEqualTo(200);
    assertThat(cancelled.body()).contains("\"status\":\"CANCELLED\"");
    assertThat(cancelled.body()).contains("\"cancellationReason\":\"No show\"");
    assertThat(send(HttpMethod.PUT, url, veterinarianToken, "{\"reason\": \"Again\"}").status())
        .isEqualTo(409);
  }

  // --------------------------------------------------------------- by pet

  @Test
  @DisplayName("A veterinarian lists the consultations of a pet, newest first")
  void listsConsultationsOfPet() throws Exception {
    var first = createConsultation(veterinarianToken);
    var second = createConsultation(veterinarianToken);

    var reply = send(HttpMethod.GET, "/api/v1/pets/5/clinical-consultations", veterinarianToken, null);

    assertThat(reply.status()).isEqualTo(200);
    var secondPosition = reply.body().indexOf("\"id\":" + second + ",");
    var firstPosition = reply.body().indexOf("\"id\":" + first + ",");
    assertThat(secondPosition >= 0).isTrue();
    assertThat(secondPosition < firstPosition).isTrue();
  }

  @Test
  @DisplayName("The consultations of a pet need authentication")
  void petConsultationsNeedAuthentication() throws Exception {
    assertThat(send(HttpMethod.GET, "/api/v1/pets/5/clinical-consultations", null, null).status())
        .isEqualTo(401);
  }
}

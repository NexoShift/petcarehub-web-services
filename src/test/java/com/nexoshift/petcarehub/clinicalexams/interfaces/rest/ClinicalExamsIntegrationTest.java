package com.nexoshift.petcarehub.clinicalexams.interfaces.rest;

import com.nexoshift.petcarehub.support.PetCareApiClient;
import com.nexoshift.petcarehub.support.PetCareApiClient.ClinicSetup;
import com.nexoshift.petcarehub.support.PetCareApiClient.OwnerSetup;
import com.nexoshift.petcarehub.support.PetCareApiClient.Reply;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * API de Clinical Exams de punta a punta (H2).
 * Escenario: "clinic" con su vet y "owner" con su mascota (paciente de clinic) y una consulta
 * IN_PROGRESS. "otherClinic" y "otherOwner" son ajenos.
 */
@SpringBootTest(properties = "spring.profiles.active=test")
class ClinicalExamsIntegrationTest {

  private static final String EXAMS = "/api/v1/clinical-exams";

  @Autowired
  private WebApplicationContext context;

  private PetCareApiClient api;
  private ClinicSetup clinic;
  private ClinicSetup otherClinic;
  private OwnerSetup owner;
  private OwnerSetup otherOwner;
  private long consultationId;

  @BeforeEach
  void setUp() throws Exception {
    api = new PetCareApiClient(MockMvcBuilders.webAppContextSetup(context)
        .apply(SecurityMockMvcConfigurers.springSecurity()).build());
    clinic = api.clinicWithVeterinarian();
    otherClinic = api.clinicWithVeterinarian();
    owner = api.ownerWithPet();
    otherOwner = api.ownerWithPet();
    api.registerPatient(clinic, owner.petId());
    consultationId = newConsultation(true);
  }

  // ------------------------------------------------------------------ helpers

  private String vet() {
    return clinic.holder().token();
  }

  private long newConsultation(boolean start) throws Exception {
    var created = api.send(HttpMethod.POST, "/api/v1/clinical-consultations", vet(),
        "{\"petId\": %d, \"clinicId\": %d, \"consultingRoomId\": %d, \"reason\": \"Cojera\"}"
            .formatted(owner.petId(), clinic.clinicId(), clinic.consultingRoomId()));
    assertThat(created.status()).as(created.body()).isEqualTo(201);
    var id = created.number("id");
    if (start) {
      var started = api.send(HttpMethod.PUT,
          "/api/v1/clinical-consultations/" + id + "/start", vet(), null);
      assertThat(started.status()).as(started.body()).isEqualTo(200);
    }
    return id;
  }

  private Reply request(String token, long consultation, String type) throws Exception {
    return api.send(HttpMethod.POST, EXAMS, token,
        "{\"consultationId\": %d, \"type\": \"%s\", \"indication\": \"  Tórax lateral  \"}"
            .formatted(consultation, type));
  }

  private long requestedExam() throws Exception {
    var reply = request(vet(), consultationId, "RADIOGRAPHY");
    assertThat(reply.status()).as(reply.body()).isEqualTo(201);
    return reply.number("id");
  }

  private Reply put(String token, long examId, String action) throws Exception {
    return api.send(HttpMethod.PUT, EXAMS + "/" + examId + "/" + action, token, null);
  }

  private Reply result(String token, long examId, LocalDate date) throws Exception {
    return api.send(HttpMethod.POST, EXAMS + "/" + examId + "/result", token,
        "{\"summary\": \"Fractura de radio\", \"values\": \"-\", "
            + "\"interpretation\": \"Inmovilizar\", \"resultDate\": \"" + date + "\"}");
  }

  private Reply upload(String token, long examId, String name, String type, byte[] content)
      throws Exception {
    return api.sendFile(EXAMS + "/" + examId + "/files", token,
        new MockMultipartFile("file", name, type, content), Map.of());
  }

  // ------------------------------------------------------------------ solicitar

  @Test
  @DisplayName("A vet requests an exam in a consultation in progress: REQUESTED, pet from the consultation")
  void requestsExam() throws Exception {
    var reply = request(vet(), consultationId, "RADIOGRAPHY");

    assertThat(reply.status()).as(reply.body()).isEqualTo(201);
    assertThat(reply.location()).isEqualTo(EXAMS + "/" + reply.number("id"));
    assertThat(reply.text("status")).isEqualTo("REQUESTED");
    assertThat(reply.text("indication")).isEqualTo("Tórax lateral");
    assertThat(reply.number("petId")).isEqualTo(owner.petId());
    assertThat(reply.number("consultationId")).isEqualTo(consultationId);
    assertThat(reply.number("clinicId")).isEqualTo(clinic.clinicId());
    assertThat(reply.number("requestedBy")).isEqualTo(clinic.veterinarianId());
  }

  @Test
  @DisplayName("Requesting needs a consultation in progress (409) that exists (404) and valid data (400)")
  void requestRules() throws Exception {
    assertThat(request(vet(), newConsultation(false), "LABORATORY").status()).isEqualTo(409);
    assertThat(request(vet(), 999_999, "LABORATORY").status()).isEqualTo(404);
    assertThat(request(vet(), consultationId, "MRI").status()).isEqualTo(400);
    assertThat(api.send(HttpMethod.POST, EXAMS, vet(), "{\"type\": \"OTHER\"}").status())
        .isEqualTo(400);
  }

  @Test
  @DisplayName("Only a vet of the consultation's clinic can request: other clinic 403, owner 403, no token 401")
  void requestPermissions() throws Exception {
    assertThat(request(otherClinic.holder().token(), consultationId, "OTHER").status())
        .isEqualTo(403);
    assertThat(request(owner.owner().token(), consultationId, "OTHER").status()).isEqualTo(403);
    assertThat(request(null, consultationId, "OTHER").status()).isEqualTo(401);
  }

  @Test
  @DisplayName("A member vet of the same clinic can request and work the exam")
  void memberVet() throws Exception {
    var member = api.addVeterinarian(clinic);
    var reply = request(member.member().token(), consultationId, "ULTRASOUND");

    assertThat(reply.status()).as(reply.body()).isEqualTo(201);
    assertThat(reply.number("requestedBy")).isEqualTo(member.veterinarianId());
    assertThat(put(member.member().token(), reply.number("id"), "start").status()).isEqualTo(200);
  }

  // ------------------------------------------------------------------ ciclo de vida

  @Test
  @DisplayName("Full flow: start, result, validate, complete; files allowed after start")
  void fullFlow() throws Exception {
    var examId = requestedExam();

    var started = put(vet(), examId, "start");
    assertThat(started.status()).isEqualTo(200);
    assertThat(started.text("status")).isEqualTo("IN_PROCESS");

    var file = upload(vet(), examId, "placa.png", "image/png", new byte[] {1, 2, 3});
    assertThat(file.status()).as(file.body()).isEqualTo(201);
    assertThat(file.text("type")).isEqualTo("IMAGE");
    assertThat(file.number("sizeBytes")).isEqualTo(3);

    var withResult = result(vet(), examId, LocalDate.now());
    assertThat(withResult.status()).as(withResult.body()).isEqualTo(201);
    assertThat(withResult.body()).contains("\"validated\":false");

    var validated = put(vet(), examId, "result/validate");
    assertThat(validated.status()).isEqualTo(200);
    assertThat(validated.body()).contains("\"validated\":true")
        .contains("\"validatedBy\":" + clinic.veterinarianId());

    var completed = put(vet(), examId, "complete");
    assertThat(completed.status()).isEqualTo(200);
    assertThat(completed.text("status")).isEqualTo("COMPLETED");
    assertThat(completed.text("completedAt")).isNotBlank();

    var pdf = upload(vet(), examId, "informe.pdf", "application/pdf", "PDF".getBytes());
    assertThat(pdf.status()).isEqualTo(201);
    assertThat(pdf.text("type")).isEqualTo("DOCUMENT");

    var detail = api.send(HttpMethod.GET, EXAMS + "/" + examId, vet(), null);
    assertThat(detail.body()).contains("placa.png").contains("informe.pdf");
  }

  @Test
  @DisplayName("State rules answer 409: start twice, result before start or twice, complete without validation")
  void stateRules() throws Exception {
    var examId = requestedExam();
    assertThat(result(vet(), examId, LocalDate.now()).status()).isEqualTo(409);
    assertThat(put(vet(), examId, "complete").status()).isEqualTo(409);
    assertThat(put(vet(), examId, "result/validate").status()).isEqualTo(409);

    put(vet(), examId, "start");
    assertThat(put(vet(), examId, "start").status()).isEqualTo(409);
    assertThat(put(vet(), examId, "result/validate").status()).isEqualTo(409);

    result(vet(), examId, LocalDate.now());
    assertThat(result(vet(), examId, LocalDate.now()).status()).isEqualTo(409);
    assertThat(put(vet(), examId, "complete").status()).isEqualTo(409);
  }

  @Test
  @DisplayName("The result needs a summary and a date that is not in the future (400)")
  void resultValidation() throws Exception {
    var examId = requestedExam();
    put(vet(), examId, "start");

    assertThat(result(vet(), examId, LocalDate.now().plusDays(1)).status()).isEqualTo(400);
    assertThat(api.send(HttpMethod.POST, EXAMS + "/" + examId + "/result", vet(),
        "{\"summary\": \" \", \"resultDate\": \"" + LocalDate.now() + "\"}").status())
        .isEqualTo(400);
  }

  @Test
  @DisplayName("Files: not before start (409), only image/video/PDF (400), not empty (400)")
  void fileRules() throws Exception {
    var examId = requestedExam();
    assertThat(upload(vet(), examId, "a.png", "image/png", new byte[] {1}).status())
        .isEqualTo(409);

    put(vet(), examId, "start");
    assertThat(upload(vet(), examId, "a.txt", "text/plain", new byte[] {1}).status())
        .isEqualTo(400);
    assertThat(upload(vet(), examId, "a.png", "image/png", new byte[0]).status())
        .isEqualTo(400);
    assertThat(upload(vet(), examId, "video.mp4", "video/mp4", new byte[] {1}).status())
        .isEqualTo(201);
  }

  @Test
  @DisplayName("Cancel with a reason from REQUESTED or IN_PROCESS; never a completed exam")
  void cancel() throws Exception {
    var examId = requestedExam();
    assertThat(api.send(HttpMethod.PUT, EXAMS + "/" + examId + "/cancel", vet(),
        "{\"reason\": \" \"}").status()).isEqualTo(400);

    var cancelled = api.send(HttpMethod.PUT, EXAMS + "/" + examId + "/cancel", vet(),
        "{\"reason\": \"El tutor no vino\"}");
    assertThat(cancelled.status()).isEqualTo(200);
    assertThat(cancelled.text("status")).isEqualTo("CANCELLED");
    assertThat(cancelled.text("cancellationReason")).isEqualTo("El tutor no vino");
    assertThat(put(vet(), examId, "start").status()).isEqualTo(409);
  }

  @Test
  @DisplayName("The exam keeps its life cycle after the consultation is cancelled")
  void outlivesConsultation() throws Exception {
    var examId = requestedExam();
    api.send(HttpMethod.PUT, "/api/v1/clinical-consultations/" + consultationId + "/cancel",
        vet(), "{\"reason\": \"Se fue\"}");

    assertThat(put(vet(), examId, "start").status()).isEqualTo(200);
  }

  // ------------------------------------------------------------------ permisos

  @Test
  @DisplayName("Another clinic cannot read, change or validate the exam (403); unknown exam 404")
  void otherClinic() throws Exception {
    var examId = requestedExam();
    var other = otherClinic.holder().token();

    assertThat(api.send(HttpMethod.GET, EXAMS + "/" + examId, other, null).status())
        .isEqualTo(403);
    assertThat(put(other, examId, "start").status()).isEqualTo(403);
    put(vet(), examId, "start");
    result(vet(), examId, LocalDate.now());
    assertThat(put(other, examId, "result/validate").status()).isEqualTo(403);
    assertThat(api.send(HttpMethod.GET,
        "/api/v1/clinical-consultations/" + consultationId + "/clinical-exams", other, null)
        .status()).isEqualTo(403);
    assertThat(api.send(HttpMethod.GET, EXAMS + "/999999", vet(), null).status())
        .isEqualTo(404);
  }

  @Test
  @DisplayName("The owner reads the exams of their pet and downloads files, but cannot change them")
  void ownerReadsOnly() throws Exception {
    var examId = requestedExam();
    put(vet(), examId, "start");
    var fileId = upload(vet(), examId, "informe.pdf", "application/pdf",
        "hola".getBytes(StandardCharsets.UTF_8)).number("id");
    var token = owner.owner().token();

    assertThat(api.send(HttpMethod.GET, EXAMS + "/" + examId, token, null).status())
        .isEqualTo(200);
    var byConsultation = api.send(HttpMethod.GET,
        "/api/v1/clinical-consultations/" + consultationId + "/clinical-exams", token, null);
    assertThat(byConsultation.status()).isEqualTo(200);
    assertThat(byConsultation.body()).contains("\"id\":" + examId);
    var byPet = api.send(HttpMethod.GET,
        "/api/v1/pets/" + owner.petId() + "/clinical-exams", token, null);
    assertThat(byPet.status()).isEqualTo(200);
    assertThat(byPet.body()).contains("\"id\":" + examId);

    var download = api.send(HttpMethod.GET, EXAMS + "/" + examId + "/files/" + fileId, token,
        null);
    assertThat(download.status()).isEqualTo(200);
    assertThat(download.body()).isEqualTo("hola");

    assertThat(put(token, examId, "complete").status()).isEqualTo(403);
    assertThat(upload(token, examId, "x.png", "image/png", new byte[] {1}).status())
        .isEqualTo(403);
  }

  @Test
  @DisplayName("The owner sees the result only after a veterinarian validates it")
  void ownerSeesOnlyValidatedResults() throws Exception {
    var examId = requestedExam();
    put(vet(), examId, "start");
    result(vet(), examId, LocalDate.now());
    var url = EXAMS + "/" + examId;
    var token = owner.owner().token();

    assertThat(api.send(HttpMethod.GET, url, vet(), null).body()).contains("Fractura de radio");
    assertThat(api.send(HttpMethod.GET, url, token, null).body()).contains("\"result\":null");
    assertThat(api.send(HttpMethod.GET,
        "/api/v1/pets/" + owner.petId() + "/clinical-exams", token, null).body())
        .doesNotContain("Fractura de radio");

    put(vet(), examId, "result/validate");
    assertThat(api.send(HttpMethod.GET, url, token, null).body()).contains("Fractura de radio");
  }

  @Test
  @DisplayName("Another owner cannot see the exams of my pet (403)")
  void otherOwner() throws Exception {
    var examId = requestedExam();
    var token = otherOwner.owner().token();

    assertThat(api.send(HttpMethod.GET, EXAMS + "/" + examId, token, null).status())
        .isEqualTo(403);
    assertThat(api.send(HttpMethod.GET,
        "/api/v1/pets/" + owner.petId() + "/clinical-exams", token, null).status())
        .isEqualTo(403);
    assertThat(api.send(HttpMethod.GET,
        "/api/v1/clinical-consultations/" + consultationId + "/clinical-exams", token, null)
        .status()).isEqualTo(403);
  }

  @Test
  @DisplayName("Lists are newest first; a vet sees a pet's exams only if it is a patient of the clinic")
  void listsByPet() throws Exception {
    var first = requestedExam();
    var second = request(vet(), consultationId, "LABORATORY").number("id");

    var mine = api.send(HttpMethod.GET, "/api/v1/pets/" + owner.petId() + "/clinical-exams",
        vet(), null);
    assertThat(mine.status()).isEqualTo(200);
    assertThat(mine.body().indexOf("\"id\":" + second))
        .isLessThan(mine.body().indexOf("\"id\":" + first));
    assertThat(api.send(HttpMethod.GET, "/api/v1/pets/" + owner.petId() + "/clinical-exams",
        otherClinic.holder().token(), null).status()).isEqualTo(403);
  }

  @Test
  @DisplayName("Worklist: each clinic sees only its own exams, filtered by status; owners cannot")
  void worklistOfTheClinic() throws Exception {
    var requested = requestedExam();
    var inProcess = requestedExam();
    put(vet(), inProcess, "start");

    var all = api.send(HttpMethod.GET, EXAMS, vet(), null);
    assertThat(all.status()).isEqualTo(200);
    assertThat(all.body()).contains("\"id\":" + requested).contains("\"id\":" + inProcess);

    var pending = api.send(HttpMethod.GET, EXAMS + "?status=REQUESTED", vet(), null);
    assertThat(pending.body()).contains("\"id\":" + requested)
        .doesNotContain("\"id\":" + inProcess + ",");

    var other = api.send(HttpMethod.GET, EXAMS, otherClinic.holder().token(), null);
    assertThat(other.status()).isEqualTo(200);
    assertThat(other.body()).isEqualTo("[]");

    assertThat(api.send(HttpMethod.GET, EXAMS, owner.owner().token(), null).status())
        .isEqualTo(403);
    assertThat(api.send(HttpMethod.GET, EXAMS + "?status=OPEN", vet(), null).status())
        .isEqualTo(400);
  }

  @Test
  @DisplayName("Unknown file of an exam is 404")
  void unknownFile() throws Exception {
    var examId = requestedExam();
    assertThat(api.send(HttpMethod.GET, EXAMS + "/" + examId + "/files/999", vet(), null)
        .status()).isEqualTo(404);
  }
}

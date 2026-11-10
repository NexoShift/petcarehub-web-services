package com.nexoshift.petcarehub.aiassistance.interfaces.rest;

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

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * NUEVO. Pruebas de AI Assistance (análisis de imágenes) con Profiles y Clinical Attention
 * reales (ai.acl.mode=facade), el modelo de visión "fake" y almacenamiento local en target/.
 * <pre>
 *   clinic      : clínica con vet; "owner.pet" es su paciente
 *   otherClinic : otra clínica con su vet; la mascota NO es su paciente
 *   owner       : dueño de la mascota;  stranger: otro dueño
 * </pre>
 */
@SpringBootTest(properties = {
    "spring.profiles.active=test",
    "ai.acl.mode=facade",
    "ai.vision.provider=fake",
    "ai.storage.provider=local",
    "ai.storage.local-dir=target/test-uploads"})
class VisionAnalysesControllerIntegrationTest {

  private static final String BASE = "/api/v1/ai/vision-analyses";

  @Autowired
  private WebApplicationContext context;

  private PetCareApiClient api;
  private ClinicSetup clinic;
  private ClinicSetup otherClinic;
  private OwnerSetup owner;
  private OwnerSetup stranger;

  @BeforeEach
  void setUp() throws Exception {
    var mockMvc = MockMvcBuilders.webAppContextSetup(context)
        .apply(SecurityMockMvcConfigurers.springSecurity())
        .build();
    api = new PetCareApiClient(mockMvc);
    clinic = api.clinicWithVeterinarian();
    otherClinic = api.clinicWithVeterinarian();
    owner = api.ownerWithPet();
    stranger = api.ownerWithPet();
    api.registerPatient(clinic, owner.petId());
  }

  // ------------------------------------------------------------------ helpers

  /** PNG de 300x300 con color de piel (pasa el ImageValidator: mínimo 224 px). */
  private static MockMultipartFile skinPhoto() throws Exception {
    var image = new BufferedImage(300, 300, BufferedImage.TYPE_INT_RGB);
    var graphics = image.createGraphics();
    graphics.setColor(new Color(220, 160, 140));
    graphics.fillRect(0, 0, 300, 300);
    graphics.setColor(new Color(180, 60, 60));
    graphics.fillOval(100, 100, 80, 60);
    graphics.dispose();
    var bytes = new ByteArrayOutputStream();
    ImageIO.write(image, "png", bytes);
    return new MockMultipartFile("file", "piel.png", "image/png", bytes.toByteArray());
  }

  private Reply upload(String token, long petId, Long consultationId) throws Exception {
    var params = consultationId == null
        ? Map.of("petId", petId, "imageType", "SKIN")
        : Map.of("petId", petId, "imageType", "SKIN", "consultationId", consultationId);
    return api.sendFile(BASE, token, skinPhoto(), params);
  }

  private long consultation(boolean started) throws Exception {
    var created = api.send(HttpMethod.POST, "/api/v1/clinical-consultations",
        clinic.holder().token(), """
            {"petId": %d, "clinicId": %d, "reason": "Lesión en la piel"}"""
            .formatted(owner.petId(), clinic.clinicId()));
    assertThat(created.status()).as(created.body()).isEqualTo(201);
    var id = created.number("id");
    if (started) {
      assertThat(api.send(HttpMethod.PUT, "/api/v1/clinical-consultations/" + id + "/start",
          clinic.holder().token(), null).status()).isEqualTo(200);
    }
    return id;
  }

  // ------------------------------------------------------------------ owner

  @Test
  @DisplayName("The owner analyzes a skin photo of their pet; another owner cannot")
  void ownerUploads() throws Exception {
    var mine = upload(owner.owner().token(), owner.petId(), null);
    var notMine = upload(stranger.owner().token(), owner.petId(), null);

    assertThat(mine.status()).as(mine.body()).isEqualTo(201);
    assertThat(notMine.status()).isEqualTo(403);
  }

  @Test
  @DisplayName("Another owner cannot see the analyses of my pet")
  void ownerReads() throws Exception {
    var id = upload(owner.owner().token(), owner.petId(), null).number("id");

    assertThat(api.send(HttpMethod.GET, BASE + "/" + id, owner.owner().token(), null).status())
        .isEqualTo(200);
    assertThat(api.send(HttpMethod.GET, BASE + "/" + id, stranger.owner().token(), null).status())
        .isEqualTo(403);
    assertThat(api.send(HttpMethod.GET, BASE + "?petId=" + owner.petId(),
        stranger.owner().token(), null).status()).isEqualTo(403);
  }

  // ------------------------------------------------------------ veterinarian

  @Test
  @DisplayName("NUEVO: a vet analyzes a patient of the clinic; a vet of another clinic gets 403")
  void veterinarianUploads() throws Exception {
    var ours = upload(clinic.holder().token(), owner.petId(), null);
    var theirs = upload(otherClinic.holder().token(), owner.petId(), null);

    assertThat(ours.status()).as(ours.body()).isEqualTo(201);
    assertThat(theirs.status()).isEqualTo(403);
  }

  @Test
  @DisplayName("NUEVO: a vet of another clinic cannot list or open the analyses of the pet")
  void veterinarianOfOtherClinicCannotRead() throws Exception {
    var id = upload(owner.owner().token(), owner.petId(), null).number("id");
    var otherToken = otherClinic.holder().token();

    assertThat(api.send(HttpMethod.GET, BASE + "?petId=" + owner.petId(), otherToken, null)
        .status()).isEqualTo(403);
    assertThat(api.send(HttpMethod.GET, BASE + "/" + id, otherToken, null).status())
        .isEqualTo(403);
    assertThat(api.send(HttpMethod.GET, BASE + "?petId=" + owner.petId(),
        clinic.holder().token(), null).status()).isEqualTo(200);
  }

  // ------------------------------------------------------------ consultation

  @Test
  @DisplayName("NUEVO: the consultation must exist, be of the same pet and be in progress")
  void validatesConsultation() throws Exception {
    var token = clinic.holder().token();
    var otherPet = api.ownerWithPet();
    api.registerPatient(clinic, otherPet.petId());
    var notStarted = consultation(false);
    var started = consultation(true);

    assertThat(upload(token, owner.petId(), 999_999L).status()).isEqualTo(404);
    assertThat(upload(token, otherPet.petId(), started).status()).isEqualTo(400);
    assertThat(upload(token, owner.petId(), notStarted).status()).isEqualTo(409);

    var ok = upload(token, owner.petId(), started);
    assertThat(ok.status()).as(ok.body()).isEqualTo(201);
    assertThat(ok.number("consultationId")).isEqualTo(started);
  }

  @Test
  @DisplayName("NUEVO: the analyses of a consultation are only for vets of the pet's clinic")
  void listsByConsultation() throws Exception {
    var started = consultation(true);
    upload(clinic.holder().token(), owner.petId(), started);
    var url = BASE + "?consultationId=" + started;

    var ours = api.send(HttpMethod.GET, url, clinic.holder().token(), null);
    assertThat(ours.status()).isEqualTo(200);
    assertThat(ours.body()).contains("\"consultationId\":" + started);
    assertThat(api.send(HttpMethod.GET, url, otherClinic.holder().token(), null).status())
        .isEqualTo(403);
    assertThat(api.send(HttpMethod.GET, BASE + "?consultationId=999999",
        clinic.holder().token(), null).status()).isEqualTo(404);
  }

  // ------------------------------------------------------------------ input

  @Test
  @DisplayName("NUEVO: without the file part the API answers 400 (it was 500)")
  void missingFileIs400() throws Exception {
    var reply = api.sendFile(BASE, owner.owner().token(), null,
        Map.of("petId", owner.petId(), "imageType", "SKIN"));

    assertThat(reply.status()).isEqualTo(400);
    assertThat(reply.body()).contains("file");
  }

  @Test
  @DisplayName("A pet that does not exist is 404")
  void unknownPet() throws Exception {
    assertThat(upload(clinic.holder().token(), 999_999L, null).status()).isEqualTo(404);
  }
}

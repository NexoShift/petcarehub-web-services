package com.nexoshift.petcarehub.aiassistance.application.internal.contextbuilders;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.nexoshift.petcarehub.aiassistance.application.internal.outboundservices.acl.stub.ClinicalAttentionServiceStub;
import com.nexoshift.petcarehub.aiassistance.application.internal.outboundservices.acl.stub.ProfilesServiceStub;
import com.nexoshift.petcarehub.aiassistance.domain.model.aggregates.VisionAnalysis;
import com.nexoshift.petcarehub.aiassistance.domain.model.commands.RequestVisionAnalysisCommand;
import com.nexoshift.petcarehub.aiassistance.domain.model.valueobjects.BackgroundType;
import com.nexoshift.petcarehub.aiassistance.domain.model.valueobjects.ConfidenceScore;
import com.nexoshift.petcarehub.aiassistance.domain.model.valueobjects.ImageReference;
import com.nexoshift.petcarehub.aiassistance.domain.model.valueobjects.ImageType;
import com.nexoshift.petcarehub.aiassistance.domain.model.valueobjects.RequesterRole;
import com.nexoshift.petcarehub.aiassistance.domain.repositories.VisionAnalysisRepository;
import com.nexoshift.petcarehub.aiassistance.domain.model.valueobjects.SelectedBackground;
import com.nexoshift.petcarehub.shared.domain.exceptions.BadRequestException;
import com.nexoshift.petcarehub.shared.domain.exceptions.NotFoundException;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

/** Uses the demo stubs: Rocky is pet 1 (consultations 1-3), Luna is pet 2 (consultation 5). */
class ClinicalLlmContextBuilderTest {

  private final VisionAnalysisRepository visionRepository = Mockito.mock(VisionAnalysisRepository.class);
  private final ClinicalLlmContextBuilder builder = new ClinicalLlmContextBuilder(
      new ProfilesServiceStub(), new ClinicalAttentionServiceStub(), visionRepository);

  private static VisionAnalysis radiographOf(long petId, boolean completed) {
    var analysis = new VisionAnalysis(new RequestVisionAnalysisCommand(4L, petId, 7L,
        RequesterRole.VETERINARIAN, ImageType.RADIOGRAPH, new byte[] {1}, "image/png"),
        new ImageReference("vision/2026/11/a.png", "image/png", 1L));
    analysis.startProcessing();
    analysis.recordFinding("cardiomegalia", new ConfidenceScore(0.78), null, 0.20);
    analysis.recordFinding("efusion_pleural", new ConfidenceScore(0.41), null, 0.20);
    if (completed) {
      analysis.complete("fake/1");
    }
    return analysis;
  }

  private static SelectedBackground consultation(long id) {
    return new SelectedBackground(BackgroundType.PREVIOUS_CONSULTATION, id);
  }

  @Test
  void onlySelectedBackgroundsReachTheContext() {
    var context = builder.build(1L, "Rascado en orejas", List.of(consultation(1), consultation(2)));
    assertEquals(2, context.backgrounds().size());
    assertTrue(context.backgrounds().get(0).contains("Dermatitis atópica"));
    assertTrue(context.backgrounds().get(1).contains("Malassezia"));
    assertTrue(context.backgrounds().stream().noneMatch(text -> text.contains("Gastritis")));
  }

  @Test
  void petNameIsNotSentToTheModel() {
    var context = builder.build(1L, "Rascado", List.of());
    assertFalse(context.patientSummary().contains("Rocky"));
    assertTrue(context.patientSummary().contains("Bulldog francés"));
  }

  @Test
  void backgroundOfAnotherPetIsRejected() {
    assertThrows(BadRequestException.class,
        () -> builder.build(1L, "Rascado", List.of(consultation(5))));
  }

  @Test
  void unknownConsultationIsRejected() {
    assertThrows(BadRequestException.class,
        () -> builder.build(1L, "Rascado", List.of(consultation(99))));
  }

  @Test
  void unknownPetIsNotFound() {
    assertThrows(NotFoundException.class, () -> builder.build(42L, "Rascado", List.of()));
  }

  @Test
  void completedVisionAnalysisSendsOnlyProbableFindings() {
    Mockito.when(visionRepository.findById(9L)).thenReturn(Optional.of(radiographOf(1L, true)));
    var vision = new SelectedBackground(BackgroundType.VISION_ANALYSIS, 9L);

    var context = builder.build(1L, "Tos", List.of(vision));

    var text = context.backgrounds().getFirst();
    assertTrue(text.startsWith("Análisis de radiografía del"));
    assertTrue(text.contains("cardiomegalia (78 %)"));
    assertFalse(text.contains("efusion_pleural"));
  }

  @Test
  void visionAnalysisOfAnotherPetIsRejected() {
    Mockito.when(visionRepository.findById(9L)).thenReturn(Optional.of(radiographOf(2L, true)));
    var vision = new SelectedBackground(BackgroundType.VISION_ANALYSIS, 9L);
    assertThrows(BadRequestException.class, () -> builder.build(1L, "Tos", List.of(vision)));
  }

  @Test
  void unfinishedVisionAnalysisIsRejected() {
    Mockito.when(visionRepository.findById(9L)).thenReturn(Optional.of(radiographOf(1L, false)));
    var vision = new SelectedBackground(BackgroundType.VISION_ANALYSIS, 9L);
    assertThrows(BadRequestException.class, () -> builder.build(1L, "Tos", List.of(vision)));
  }
}

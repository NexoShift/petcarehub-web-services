package com.nexoshift.petcarehub.aiassistance.domain;

import com.nexoshift.petcarehub.aiassistance.domain.exceptions.InvalidAiAnalysisStateException;
import com.nexoshift.petcarehub.aiassistance.domain.model.aggregates.VisionAnalysis;
import com.nexoshift.petcarehub.aiassistance.domain.model.commands.RequestVisionAnalysisCommand;
import com.nexoshift.petcarehub.aiassistance.domain.model.events.VisionAnalysisCompletedEvent;
import com.nexoshift.petcarehub.aiassistance.domain.model.valueobjects.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.AbstractAggregateRoot;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Ciclo de vida del agregado VisionAnalysis: PENDING -> PROCESSING -> COMPLETED o FAILED. */
class VisionAnalysisTest {

  private static final BoundingBox BOX = new BoundingBox(0.3, 0.25, 0.35, 0.3);
  private static final ImageReference IMAGE =
      new ImageReference("vision/2026/11/foto.png", "image/png", 1234L);

  private static VisionAnalysis analysis(ImageType type) {
    var command = new RequestVisionAnalysisCommand(4L, 5L, 10L, RequesterRole.VETERINARIAN,
        type, new byte[] {1}, "image/png");
    return new VisionAnalysis(command, IMAGE);
  }

  private static VisionAnalysis processing(ImageType type) {
    var analysis = analysis(type);
    analysis.startProcessing();
    return analysis;
  }

  @SuppressWarnings("unchecked")
  private static List<Object> events(VisionAnalysis analysis) throws Exception {
    Method method = AbstractAggregateRoot.class.getDeclaredMethod("domainEvents");
    method.setAccessible(true);
    return List.copyOf((Collection<Object>) method.invoke(analysis));
  }

  @Test
  @DisplayName("A new analysis is PENDING and keeps the pet, the consultation and the image")
  void startsPending() {
    var analysis = analysis(ImageType.SKIN);

    assertThat(analysis.getStatus()).isEqualTo(AiAnalysisStatus.PENDING);
    assertThat(analysis.getPetId()).isEqualTo(5L);
    assertThat(analysis.getConsultationId()).isEqualTo(4L);
    assertThat(analysis.getImage()).isEqualTo(IMAGE);
    assertThatThrownBy(() -> new VisionAnalysis(new RequestVisionAnalysisCommand(null, 5L, 10L,
        RequesterRole.VETERINARIAN, ImageType.SKIN, new byte[] {1}, "image/png"), null))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  @DisplayName("It can only start processing once")
  void startsOnce() {
    var analysis = processing(ImageType.SKIN);

    assertThat(analysis.getStatus()).isEqualTo(AiAnalysisStatus.PROCESSING);
    assertThatThrownBy(analysis::startProcessing)
        .isInstanceOf(InvalidAiAnalysisStateException.class);
  }

  @Test
  @DisplayName("Findings below the threshold are discarded")
  void threshold() {
    var analysis = processing(ImageType.SKIN);

    assertThat(analysis.recordFinding("dermatitis", new ConfidenceScore(0.82), BOX, 0.5)).isTrue();
    assertThat(analysis.recordFinding("micosis", new ConfidenceScore(0.31), BOX, 0.5)).isFalse();
    assertThat(analysis.getFindings()).extracting("label").containsExactly("dermatitis");
  }

  @Test
  @DisplayName("Skin findings need a location; radiograph findings never keep one")
  void locations() {
    var skin = processing(ImageType.SKIN);
    assertThatThrownBy(() -> skin.recordFinding("dermatitis", new ConfidenceScore(0.9), null, 0.5))
        .isInstanceOf(IllegalArgumentException.class);

    var radiograph = processing(ImageType.RADIOGRAPH);
    radiograph.recordFinding("cardiomegalia", new ConfidenceScore(0.78), BOX, 0.2);
    assertThat(radiograph.getFindings().get(0).getBoundingBox()).isNull();
  }

  @Test
  @DisplayName("Findings are returned from the most to the least confident")
  void sortedFindings() {
    var analysis = processing(ImageType.RADIOGRAPH);
    analysis.recordFinding("efusion_pleural", new ConfidenceScore(0.41), null, 0.2);
    analysis.recordFinding("cardiomegalia", new ConfidenceScore(0.78), null, 0.2);

    assertThat(analysis.getFindings()).extracting("label")
        .containsExactly("cardiomegalia", "efusion_pleural");
  }

  @Test
  @DisplayName("Completing keeps the model and publishes VisionAnalysisCompletedEvent")
  void completes() throws Exception {
    var analysis = processing(ImageType.SKIN);
    analysis.recordFinding("dermatitis", new ConfidenceScore(0.82), BOX, 0.5);

    analysis.complete("fake-piel/1");

    assertThat(analysis.isCompleted()).isTrue();
    assertThat(analysis.getModelId()).isEqualTo("fake-piel/1");
    assertThat(events(analysis)).singleElement()
        .isInstanceOfSatisfying(VisionAnalysisCompletedEvent.class,
            event -> assertThat(event.findings()).isEqualTo(1));
    assertThatThrownBy(() -> analysis.recordFinding("x", new ConfidenceScore(0.9), BOX, 0.5))
        .isInstanceOf(InvalidAiAnalysisStateException.class);
  }

  @Test
  @DisplayName("Failing keeps the reason (at most 500 characters)")
  void fails() {
    var analysis = processing(ImageType.SKIN);

    analysis.fail("x".repeat(800));

    assertThat(analysis.getStatus()).isEqualTo(AiAnalysisStatus.FAILED);
    assertThat(analysis.getFailureReason()).hasSize(500);
    assertThatThrownBy(() -> analysis.complete("m"))
        .isInstanceOf(InvalidAiAnalysisStateException.class);
  }

  @Test
  @DisplayName("A pending analysis cannot complete or fail")
  void pendingCannotFinish() {
    var analysis = analysis(ImageType.SKIN);

    assertThatThrownBy(() -> analysis.complete("m"))
        .isInstanceOf(InvalidAiAnalysisStateException.class);
    assertThatThrownBy(() -> analysis.fail("x"))
        .isInstanceOf(InvalidAiAnalysisStateException.class);
  }

  @Test
  @DisplayName("The summaries only mention probable findings (50 % or more)")
  void summaries() {
    var analysis = processing(ImageType.RADIOGRAPH);
    analysis.recordFinding("cardiomegalia", new ConfidenceScore(0.78), null, 0.2);
    analysis.recordFinding("efusion_pleural", new ConfidenceScore(0.41), null, 0.2);

    assertThat(analysis.findingsSummary()).isEqualTo("cardiomegalia 78 %");
    assertThat(analysis.toBackgroundText())
        .startsWith("Análisis de radiografía del ")
        .contains("cardiomegalia (78 %)")
        .doesNotContain("efusion_pleural");

    var empty = processing(ImageType.SKIN);
    assertThat(empty.findingsSummary()).isEqualTo("Sin hallazgos con confianza suficiente");
  }
}

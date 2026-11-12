package com.nexoshift.petcarehub.aiassistance.domain;

import com.nexoshift.petcarehub.aiassistance.domain.model.commands.RequestVisionAnalysisCommand;
import com.nexoshift.petcarehub.aiassistance.domain.model.valueobjects.*;
import com.nexoshift.petcarehub.shared.domain.exceptions.ForbiddenException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Value objects y el comando de análisis de imagen. */
class AiValueObjectsTest {

  private static RequestVisionAnalysisCommand command(Long consultationId, RequesterRole role,
      ImageType type, byte[] image, String contentType) {
    return new RequestVisionAnalysisCommand(consultationId, 5L, 10L, role, type, image,
        contentType);
  }

  @Test
  @DisplayName("Confidence goes from 0 to 1 and is probable from 50 %")
  void confidence() {
    assertThatThrownBy(() -> new ConfidenceScore(1.2)).isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> new ConfidenceScore(-0.1)).isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> new ConfidenceScore(Double.NaN))
        .isInstanceOf(IllegalArgumentException.class);
    assertThat(new ConfidenceScore(0.5).isProbable()).isTrue();
    assertThat(new ConfidenceScore(0.49).isProbable()).isFalse();
    assertThat(new ConfidenceScore(0.826).asPercentage()).isEqualTo(83);
  }

  @Test
  @DisplayName("A bounding box is kept inside the image (fractions from 0 to 1)")
  void boundingBox() {
    var box = new BoundingBox(-0.2, 0.9, 0.5, 0.5);

    assertThat(box.left()).isZero();
    assertThat(box.top()).isEqualTo(0.9);
    assertThat(box.height()).isCloseTo(0.1, org.assertj.core.data.Offset.offset(1e-9));
    assertThatThrownBy(() -> new BoundingBox(null, 0.1, 0.1, 0.1))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  @DisplayName("The image reference needs its storage key")
  void imageReference() {
    assertThatThrownBy(() -> new ImageReference(" ", "image/png", 1L))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  @DisplayName("Pet owners only upload skin photos and never link a consultation")
  void ownerRules() {
    assertThatThrownBy(() -> command(null, RequesterRole.PET_OWNER, ImageType.RADIOGRAPH,
        new byte[] {1}, "image/png")).isInstanceOf(ForbiddenException.class);

    var owner = command(4L, RequesterRole.PET_OWNER, ImageType.SKIN, new byte[] {1}, "image/png");
    assertThat(owner.consultationId()).isNull();
    var vet = command(4L, RequesterRole.VETERINARIAN, ImageType.SKIN, new byte[] {1}, "image/png");
    assertThat(vet.consultationId()).isEqualTo(4L);
  }

  @Test
  @DisplayName("Only JPG and PNG are accepted, and the image is required")
  void fileRules() {
    assertThatThrownBy(() -> command(null, RequesterRole.VETERINARIAN, ImageType.SKIN,
        new byte[] {1}, "image/gif")).isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> command(null, RequesterRole.VETERINARIAN, ImageType.SKIN,
        new byte[0], "image/png")).isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> command(null, RequesterRole.VETERINARIAN, null,
        new byte[] {1}, "image/png")).isInstanceOf(IllegalArgumentException.class);
    assertThat(command(null, RequesterRole.VETERINARIAN, ImageType.SKIN, new byte[] {1},
        "IMAGE/JPEG").contentType()).isEqualTo("image/jpeg");
  }
}

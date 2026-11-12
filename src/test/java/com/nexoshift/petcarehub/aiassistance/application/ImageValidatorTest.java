package com.nexoshift.petcarehub.aiassistance.application;

import com.nexoshift.petcarehub.aiassistance.application.internal.validation.ImageValidator;
import com.nexoshift.petcarehub.aiassistance.domain.exceptions.ImageNotEvaluableException;
import com.nexoshift.petcarehub.aiassistance.domain.model.valueobjects.ImageType;
import com.nexoshift.petcarehub.aiassistance.support.TestImages;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** El validador rechaza (422) lo que no se puede analizar antes de gastar almacenamiento o créditos. */
class ImageValidatorTest {

  private final ImageValidator validator = new ImageValidator();

  @Test
  @DisplayName("A skin photo of at least 224 px per side is valid")
  void validSkin() {
    assertThatCode(() -> validator.validate(TestImages.skin(300, 300), ImageType.SKIN))
        .doesNotThrowAnyException();
  }

  @Test
  @DisplayName("An image smaller than 224 px on any side is rejected")
  void tooSmall() {
    assertThatThrownBy(() -> validator.validate(TestImages.skin(300, 200), ImageType.SKIN))
        .isInstanceOf(ImageNotEvaluableException.class);
  }

  @Test
  @DisplayName("A grayscale radiograph is valid; a color photo sent as radiograph is not")
  void radiographMustBeGray() {
    assertThatCode(() -> validator.validate(TestImages.radiograph(300, 300), ImageType.RADIOGRAPH))
        .doesNotThrowAnyException();
    assertThatThrownBy(() -> validator.validate(TestImages.skin(300, 300), ImageType.RADIOGRAPH))
        .isInstanceOf(ImageNotEvaluableException.class);
  }

  @Test
  @DisplayName("Bytes that are not an image are rejected, whatever the content type says")
  void notAnImage() {
    assertThatThrownBy(() -> validator.validate("hola".getBytes(), ImageType.SKIN))
        .isInstanceOf(ImageNotEvaluableException.class);
  }
}

package com.nexoshift.petcarehub.aiassistance.infrastructure.vision.roboflow;

import com.nexoshift.petcarehub.aiassistance.infrastructure.vision.roboflow.configuration.RoboflowProperties;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** With ai.vision.provider=roboflow the API does not start without the key and both models. */
class RoboflowVisionModelServiceImplTest {

  private static RoboflowProperties properties(String apiKey, String skin, String radiograph) {
    return new RoboflowProperties(apiKey, "https://serverless.roboflow.com", skin, radiograph,
        Duration.ofSeconds(8));
  }

  @Test
  @DisplayName("Key and both models configured: the provider is created")
  void complete() {
    assertThatCode(() -> new RoboflowVisionModelServiceImpl(
        properties("rf_key", "project/skin/1", "project/radiographs/2")))
        .doesNotThrowAnyException();
  }

  @Test
  @DisplayName("Without the radiograph model the API does not start")
  void missingRadiographModel() {
    assertThatThrownBy(() -> new RoboflowVisionModelServiceImpl(
        properties("rf_key", "project/skin/1", " ")))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("ROBOFLOW_RADIOGRAPH_MODEL_ID");
  }

  @Test
  @DisplayName("Every missing Roboflow variable is named at once")
  void namesEveryMissingVariable() {
    assertThatThrownBy(() -> new RoboflowVisionModelServiceImpl(properties(null, "", null)))
        .hasMessageContaining("ROBOFLOW_API_KEY")
        .hasMessageContaining("ROBOFLOW_SKIN_MODEL_ID")
        .hasMessageContaining("ROBOFLOW_RADIOGRAPH_MODEL_ID");
  }
}

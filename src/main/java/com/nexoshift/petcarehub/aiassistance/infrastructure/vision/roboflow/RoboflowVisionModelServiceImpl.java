package com.nexoshift.petcarehub.aiassistance.infrastructure.vision.roboflow;

import com.nexoshift.petcarehub.aiassistance.application.internal.outboundservices.vision.VisionModelResult;
import com.nexoshift.petcarehub.aiassistance.application.internal.outboundservices.vision.VisionModelResult.Detection;
import com.nexoshift.petcarehub.aiassistance.application.internal.outboundservices.vision.VisionModelService;
import com.nexoshift.petcarehub.aiassistance.domain.exceptions.AiProviderUnavailableException;
import com.nexoshift.petcarehub.aiassistance.domain.model.valueobjects.ImageType;
import com.nexoshift.petcarehub.aiassistance.infrastructure.vision.roboflow.configuration.RoboflowProperties;
import java.net.http.HttpClient;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Comparator;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;

/**
 * Roboflow hosted API (anti-corruption layer). SKIN uses the object detection model and
 * RADIOGRAPH the classification model. The API key travels in the URL, so neither the URL
 * nor the full exception is ever logged.
 */
@Service
@ConditionalOnProperty(name = "ai.vision.provider", havingValue = "roboflow")
public class RoboflowVisionModelServiceImpl implements VisionModelService {

  private static final Logger LOGGER = LoggerFactory.getLogger(RoboflowVisionModelServiceImpl.class);
  private static final String PROVIDER = "Roboflow";

  private final RestClient restClient;
  private final RoboflowProperties properties;

  public RoboflowVisionModelServiceImpl(RoboflowProperties properties) {
    if (isBlank(properties.apiKey()) || isBlank(properties.skinModelId())) {
      throw new IllegalStateException(
          "Set ROBOFLOW_API_KEY and ROBOFLOW_SKIN_MODEL_ID or use ai.vision.provider=fake");
    }
    if (isBlank(properties.radiographModelId())) {
      LOGGER.warn("ROBOFLOW_RADIOGRAPH_MODEL_ID is empty: radiograph analyses will answer 503");
    }
    this.properties = properties;
    var httpClient = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_1_1)
            .connectTimeout(Duration.ofSeconds(3))
            .build();
    var requestFactory = new JdkClientHttpRequestFactory(httpClient);
    requestFactory.setReadTimeout(properties.timeout());
    this.restClient = RestClient.builder().requestFactory(requestFactory).build();
  }

  @Override
  public VisionModelResult detect(byte[] image, String contentType, ImageType imageType) {
    var modelId = imageType == ImageType.RADIOGRAPH
        ? properties.radiographModelId() : properties.skinModelId();
    if (isBlank(modelId)) {
      LOGGER.warn("No Roboflow model configured for {}", imageType);
      throw new AiProviderUnavailableException(PROVIDER, null);
    }
    LOGGER.warn("Calling {}/{}", properties.baseUrl(), modelId);
      try {
      var body = restClient.post()
          // the model id has a slash (project/version), so it goes in the template as text
          .uri(properties.baseUrl() + "/" + modelId + "?api_key={key}", properties.apiKey())
          .contentType(MediaType.APPLICATION_FORM_URLENCODED)
          .body(Base64.getEncoder().encodeToString(image))
          .retrieve()
          .body(JsonNode.class);
      if (body == null || !body.has("predictions")) {
        throw new AiProviderUnavailableException(PROVIDER, null);
      }
      return imageType == ImageType.RADIOGRAPH
          ? toClassificationResult(modelId, body)
          : toDetectionResult(modelId, body);
    } catch (RestClientResponseException exception) {
      LOGGER.warn("Roboflow answered {}", exception.getStatusCode());
      throw new AiProviderUnavailableException(PROVIDER, exception);
    } catch (RestClientException | JacksonException exception) {
      LOGGER.warn("Roboflow call failed: {}", exception.getClass().getSimpleName());
      throw new AiProviderUnavailableException(PROVIDER, exception);
    }
  }

  /** Object detection: x and y are the box center in pixels; converted to normalized top-left. */
  private VisionModelResult toDetectionResult(String modelId, JsonNode body) {
    double imageWidth = body.path("image").path("width").asDouble();
    double imageHeight = body.path("image").path("height").asDouble();
    if (imageWidth <= 0 || imageHeight <= 0) {
      throw new AiProviderUnavailableException(PROVIDER, null);
    }

    var detections = new ArrayList<Detection>();
    for (var prediction : body.path("predictions")) {
      double width = prediction.path("width").asDouble();
      double height = prediction.path("height").asDouble();
      detections.add(new Detection(
          prediction.path("class").asString(),
          prediction.path("confidence").asDouble(),
          (prediction.path("x").asDouble() - width / 2) / imageWidth,
          (prediction.path("y").asDouble() - height / 2) / imageHeight,
          width / imageWidth,
          height / imageHeight));
    }
    return new VisionModelResult(modelId, detections);
  }

  /** Classification: single-label answers an array, multi-label an object keyed by class. */
  private VisionModelResult toClassificationResult(String modelId, JsonNode body) {
    var predictions = body.path("predictions");
    var detections = new ArrayList<Detection>();
    if (predictions.isArray()) {
      for (var prediction : predictions) {
        detections.add(new Detection(prediction.path("class").asString(),
            prediction.path("confidence").asDouble(), null, null, null, null));
      }
    } else {
      for (var entry : predictions.properties()) {
        detections.add(new Detection(entry.getKey(),
            entry.getValue().path("confidence").asDouble(), null, null, null, null));
      }
    }
    detections.sort(Comparator.comparingDouble(Detection::confidence).reversed());
    return new VisionModelResult(modelId, List.copyOf(detections));
  }

  private static boolean isBlank(String value) {
    return value == null || value.isBlank();
  }
}

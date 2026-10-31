package com.nexoshift.petcarehub.aiassistance.infrastructure.vision.roboflow.configuration;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/** Settings of the Roboflow provider, bound from the ai.roboflow.* properties. */
@ConfigurationProperties(prefix = "ai.roboflow")
public record RoboflowProperties(
    String apiKey,
    @DefaultValue("https://serverless.roboflow.com") String baseUrl,
    String skinModelId,
    String radiographModelId,
    @DefaultValue("8s") Duration timeout) {
}

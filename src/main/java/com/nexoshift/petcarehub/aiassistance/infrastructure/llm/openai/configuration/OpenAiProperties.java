package com.nexoshift.petcarehub.aiassistance.infrastructure.llm.openai.configuration;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/** Settings of the OpenAI provider, bound from the ai.openai.* properties. */
@ConfigurationProperties(prefix = "ai.openai")
public record OpenAiProperties(
    String apiKey,
    @DefaultValue("https://api.openai.com/v1") String baseUrl,
    @DefaultValue("gpt-5.4-mini") String model,
    @DefaultValue("10s") Duration timeout,
    @DefaultValue("1200") int maxCompletionTokens) {
}

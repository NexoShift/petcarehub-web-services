package com.nexoshift.petcarehub.aiassistance.infrastructure.llm.openai.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Map;

/**
 * Body of POST /chat/completions. GPT-5.x does not accept temperature or max_tokens.
 * response_format is left out when null (plain text answers).
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ChatCompletionRequest(
    String model,
    List<ChatMessage> messages,
    @JsonProperty("response_format") Map<String, Object> responseFormat,
    @JsonProperty("max_completion_tokens") Integer maxCompletionTokens) {
}

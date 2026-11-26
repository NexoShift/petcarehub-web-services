package com.nexoshift.petcarehub.aiassistance.infrastructure.llm.openai.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ChatCompletionResponse(String model, List<Choice> choices) {

  @JsonIgnoreProperties(ignoreUnknown = true)
  public record Choice(ChatMessage message, @JsonProperty("finish_reason") String finishReason) {
  }
}

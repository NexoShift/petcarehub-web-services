package com.nexoshift.petcarehub.aiassistance.application.internal.outboundservices.llm;

import java.util.List;

/** Structured SOAP draft returned by the model. */
public record LlmCompletion(
    String subjective,
    String objective,
    String assessment,
    String plan,
    List<String> warnings,
    String model) {

  public LlmCompletion {
    warnings = warnings == null ? List.of() : List.copyOf(warnings);
  }
}

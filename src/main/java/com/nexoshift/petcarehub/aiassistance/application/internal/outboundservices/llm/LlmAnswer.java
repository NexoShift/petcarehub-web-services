package com.nexoshift.petcarehub.aiassistance.application.internal.outboundservices.llm;

/** Free text answer of the model, for the pet owner. */
public record LlmAnswer(String text, String model) {
}

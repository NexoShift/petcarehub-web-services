package com.nexoshift.petcarehub.aiassistance.application.internal.outboundservices.llm;

/** Text sent to the model: fixed instructions plus the clinical context. */
public record LlmPrompt(String systemMessage, String userMessage) {
}

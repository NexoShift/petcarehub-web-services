package com.nexoshift.petcarehub.aiassistance.application.internal.outboundservices.llm;

import com.nexoshift.petcarehub.aiassistance.domain.exceptions.AiProviderUnavailableException;

/** Port to the language model. Implemented by the fake and the OpenAI adapters. */
public interface LlmService {

  /**
   * Generates a SOAP draft.
   *
   * @throws AiProviderUnavailableException if the provider fails or exceeds the timeout
   */
  LlmCompletion generateSoapDraft(LlmPrompt prompt);

  /**
   * Answers a question of the pet owner in plain language.
   *
   * @throws AiProviderUnavailableException if the provider fails or exceeds the timeout
   */
  LlmAnswer answerOwnerQuestion(LlmPrompt prompt);
}

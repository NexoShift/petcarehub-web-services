package com.nexoshift.petcarehub.aiassistance.infrastructure.llm.fake;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.nexoshift.petcarehub.aiassistance.application.internal.outboundservices.llm.LlmPrompt;
import com.nexoshift.petcarehub.aiassistance.domain.exceptions.AiProviderUnavailableException;
import org.junit.jupiter.api.Test;

class FakeLlmServiceImplTest {

  private final FakeLlmServiceImpl fake = new FakeLlmServiceImpl(0);

  @Test
  void countsTheBackgroundsOfThePrompt() {
    var completion = fake.generateSoapDraft(
        new LlmPrompt("system", "1. Consulta del 2026-03-12\n2. Consulta del 2026-06-05"));
    assertTrue(completion.subjective().contains("2 antecedentes"));
    assertEquals("fake", completion.model());
  }

  @Test
  void failTriggerSimulatesAProviderOutage() {
    assertThrows(AiProviderUnavailableException.class,
        () -> fake.generateSoapDraft(new LlmPrompt("system", "Rascado #fail")));
  }

  @Test
  void answersTheOwnerInPlainLanguage() {
    var answer = fake.answerOwnerQuestion(new LlmPrompt("system", "¿Qué tiene Rocky?"));
    assertTrue(answer.text().contains("veterinario"));
    assertEquals("fake", answer.model());
  }

  @Test
  void failTriggerAlsoWorksInTheOwnerChat() {
    assertThrows(AiProviderUnavailableException.class,
        () -> fake.answerOwnerQuestion(new LlmPrompt("system", "¿Es grave? #fail")));
  }
}

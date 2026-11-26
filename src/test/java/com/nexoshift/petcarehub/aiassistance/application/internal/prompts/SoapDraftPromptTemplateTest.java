package com.nexoshift.petcarehub.aiassistance.application.internal.prompts;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.nexoshift.petcarehub.aiassistance.domain.model.valueobjects.ClinicalLlmContext;
import java.util.List;
import org.junit.jupiter.api.Test;

class SoapDraftPromptTemplateTest {

  private final SoapDraftPromptTemplate template = new SoapDraftPromptTemplate();

  @Test
  void userMessageHasPatientReasonAndNumberedBackgrounds() {
    var prompt = template.render(new ClinicalLlmContext("Perro, raza Bulldog", "Rascado",
        List.of("Consulta del 2026-03-12", "Consulta del 2026-06-05")));
    assertEquals(SoapDraftPromptTemplate.SYSTEM_MESSAGE, prompt.systemMessage());
    assertTrue(prompt.userMessage().startsWith("PACIENTE\nPerro, raza Bulldog"));
    assertTrue(prompt.userMessage().contains("MOTIVO ACTUAL\nRascado"));
    assertTrue(prompt.userMessage().contains("1. Consulta del 2026-03-12"));
    assertTrue(prompt.userMessage().contains("2. Consulta del 2026-06-05"));
  }

  @Test
  void noBackgroundsIsSaidExplicitly() {
    var prompt = template.render(new ClinicalLlmContext("Gato", "Vacuna", List.of()));
    assertTrue(prompt.userMessage().contains("Ninguno."));
  }

  @Test
  void systemMessageForbidsExactDoses() {
    assertTrue(SoapDraftPromptTemplate.SYSTEM_MESSAGE.contains("dosis a definir por el veterinario"));
  }
}

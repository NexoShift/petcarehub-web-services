package com.nexoshift.petcarehub.aiassistance.application.internal.prompts;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.nexoshift.petcarehub.aiassistance.application.internal.outboundservices.acl.ConsultationBackground;
import com.nexoshift.petcarehub.aiassistance.application.internal.outboundservices.acl.PetSummary;
import com.nexoshift.petcarehub.aiassistance.domain.model.aggregates.OwnerExplanation;
import com.nexoshift.petcarehub.aiassistance.domain.model.commands.RequestOwnerExplanationCommand;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;

class OwnerExplanationPromptTemplateTest {

  private static final PetSummary ROCKY =
      new PetSummary(1L, "Rocky", "Perro", "Bulldog", "macho", 38, "atigrado");
  private static final ConsultationBackground CONSULTATION = new ConsultationBackground(1L, 1L, 1L,
      LocalDate.of(2026, 3, 12), "Rascado", "Eritema", "Dermatitis atópica", "Apoquel");

  private final OwnerExplanationPromptTemplate template = new OwnerExplanationPromptTemplate();

  @Test
  void includesPetConsultationsAndQuestion() {
    var prompt = template.render(ROCKY, List.of(CONSULTATION), List.of(), "¿Qué es atopia?");

    assertTrue(prompt.userMessage().contains("Rocky"));
    assertTrue(prompt.userMessage().contains("Dermatitis atópica"));
    assertTrue(prompt.userMessage().endsWith("¿Qué es atopia?"));
    assertFalse(prompt.userMessage().contains("CONVERSACIÓN ANTERIOR"));
  }

  @Test
  void includesThePreviousTurns() {
    var turn = new OwnerExplanation(
        new RequestOwnerExplanationCommand(1L, null, 9L, "¿Es contagioso?"), "No lo es.", "fake");

    var prompt = template.render(ROCKY, List.of(), List.of(turn), "¿Y se cura?");

    assertTrue(prompt.userMessage().contains("No hay consultas registradas."));
    assertTrue(prompt.userMessage().contains("Dueño: ¿Es contagioso?"));
    assertTrue(prompt.userMessage().contains("Asistente: No lo es."));
  }

  @Test
  void systemMessageForbidsPrescribingAndAsksForSimpleLanguage() {
    var system = template.render(ROCKY, List.of(), List.of(), "x").systemMessage();
    assertTrue(system.contains("Nunca recetes"));
    assertTrue(system.contains("150 palabras"));
    assertTrue(system.contains("emergencias"));
  }
}

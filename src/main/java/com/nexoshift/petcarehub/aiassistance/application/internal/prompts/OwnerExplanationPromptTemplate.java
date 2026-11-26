package com.nexoshift.petcarehub.aiassistance.application.internal.prompts;

import com.nexoshift.petcarehub.aiassistance.domain.model.aggregates.OwnerExplanation;
import com.nexoshift.petcarehub.aiassistance.application.internal.outboundservices.acl.ConsultationBackground;
import com.nexoshift.petcarehub.aiassistance.application.internal.outboundservices.acl.PetSummary;
import com.nexoshift.petcarehub.aiassistance.application.internal.outboundservices.llm.LlmPrompt;
import java.util.List;
import org.springframework.stereotype.Component;

/** Prompt for the "Explain with AI" chat of the pet owner. */
@Component
public class OwnerExplanationPromptTemplate {

  static final String SYSTEM_MESSAGE = """
      Eres el asistente de PetCare Hub que explica a los dueños de mascotas, en Perú,
      lo que el veterinario registró en las consultas.
      Reglas:
      1. Usa lenguaje simple, cálido y breve: máximo 150 palabras, sin tecnicismos o
         explicándolos entre paréntesis.
      2. Básate solo en los datos del caso. Si no sabes algo, dilo y sugiere preguntarle
         al veterinario.
      3. Nunca recetes, nunca cambies dosis ni suspendas tratamientos, y nunca des un
         diagnóstico nuevo. Si te lo piden, explica que eso lo decide el veterinario.
      4. Si la pregunta menciona signos de alarma (dificultad para respirar, convulsiones,
         sangrado, no come ni bebe, vómitos repetidos, decaimiento fuerte), lo primero
         que dices es que acuda a su veterinario o a emergencias.
      5. Responde en español y en texto plano, sin listas largas ni formato Markdown.
      """;

  public LlmPrompt render(PetSummary pet, List<ConsultationBackground> consultations,
      List<OwnerExplanation> previousTurns, String question) {
    var user = new StringBuilder()
        .append("MASCOTA\n").append(pet.name()).append(": ").append(pet.toPromptText())
        .append("\n\nCONSULTAS REGISTRADAS POR EL VETERINARIO\n");
    if (consultations.isEmpty()) {
      user.append("No hay consultas registradas.\n");
    } else {
      consultations.forEach(consultation ->
          user.append(consultation.toPromptText()).append("\n\n"));
    }
    if (!previousTurns.isEmpty()) {
      user.append("CONVERSACIÓN ANTERIOR\n");
      previousTurns.forEach(turn -> user.append("Dueño: ").append(turn.getQuestion()).append('\n')
          .append("Asistente: ").append(turn.getAnswer()).append("\n\n"));
    }
    user.append("PREGUNTA DEL DUEÑO\n").append(question);
    return new LlmPrompt(SYSTEM_MESSAGE, user.toString());
  }
}

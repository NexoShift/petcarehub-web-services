package com.nexoshift.petcarehub.aiassistance.application.internal.prompts;

import com.nexoshift.petcarehub.aiassistance.domain.model.valueobjects.ClinicalLlmContext;
import com.nexoshift.petcarehub.aiassistance.application.internal.outboundservices.llm.LlmPrompt;
import org.springframework.stereotype.Component;

/** The prompt lives in one class so it can be tuned without touching anything else. */
@Component
public class SoapDraftPromptTemplate {

  static final String SYSTEM_MESSAGE = """
      Eres un asistente clínico para médicos veterinarios de pequeños animales en Perú.
      Redactas un BORRADOR de nota SOAP que el veterinario revisará, editará o descartará.
      Reglas:
      1. Usa solo la información del caso. No inventes signos, resultados ni antecedentes.
      2. Si falta información importante, dilo en "warnings".
      3. En "assessment" da diagnósticos diferenciales ordenados por probabilidad,
         nunca un diagnóstico definitivo.
      4. En "plan" sugiere exámenes y manejo. No indiques dosis exactas: escribe
         "dosis a definir por el veterinario".
      5. Si detectas signos de alarma o una posible reacción a un tratamiento previo,
         ponlo como primera advertencia.
      6. Escribe en español, en tono técnico y breve. Máximo 120 palabras por sección.
      """;

  public LlmPrompt render(ClinicalLlmContext context) {
    var user = new StringBuilder()
        .append("PACIENTE\n").append(context.patientSummary()).append("\n\n")
        .append("MOTIVO ACTUAL\n").append(context.currentReason()).append("\n\n")
        .append("ANTECEDENTES SELECCIONADOS POR EL VETERINARIO\n");
    if (context.backgrounds().isEmpty()) {
      user.append("Ninguno.\n");
    } else {
      for (int i = 0; i < context.backgrounds().size(); i++) {
        user.append(i + 1).append(". ").append(context.backgrounds().get(i)).append("\n\n");
      }
    }
    return new LlmPrompt(SYSTEM_MESSAGE, user.toString());
  }
}

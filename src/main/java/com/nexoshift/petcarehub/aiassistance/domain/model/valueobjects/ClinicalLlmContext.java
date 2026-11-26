package com.nexoshift.petcarehub.aiassistance.domain.model.valueobjects;

import java.util.List;

/**
 * What the LLM is allowed to know: an anonymous patient summary, the current reason
 * and only the backgrounds the veterinarian selected, already written as text.
 * It is not persisted.
 */
public record ClinicalLlmContext(
    String patientSummary,
    String currentReason,
    List<String> backgrounds) {

  public ClinicalLlmContext {
    if (patientSummary == null || patientSummary.isBlank()) {
      throw new IllegalArgumentException("Patient summary is required");
    }
    if (currentReason == null || currentReason.isBlank()) {
      throw new IllegalArgumentException("Current reason is required");
    }
    backgrounds = backgrounds == null ? List.of() : List.copyOf(backgrounds);
  }
}

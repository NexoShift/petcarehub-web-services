package com.nexoshift.petcarehub.clinicalattention.interfaces.rest.resources;

import com.nexoshift.petcarehub.clinicalattention.domain.model.valueobjects.ClinicalFieldLimits;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

/** A prescription is issued with all its medications (1 to 15). */
public record RegisterPrescriptionResource(
    @Size(max = ClinicalFieldLimits.PRESCRIPTION_INSTRUCTION) String generalInstruction,
    @NotEmpty @Size(max = ClinicalFieldLimits.MAX_MEDICATIONS)
    List<@NotNull @Valid MedicationItem> medications) {

  /** {@code indication} is optional, e.g. "give with food". */
  public record MedicationItem(
      @NotBlank @Size(max = ClinicalFieldLimits.MEDICATION_NAME) String name,
      @NotBlank @Size(max = ClinicalFieldLimits.MEDICATION_DOSAGE) String dosage,
      @NotBlank @Size(max = ClinicalFieldLimits.MEDICATION_FREQUENCY) String frequency,
      @NotBlank @Size(max = ClinicalFieldLimits.MEDICATION_DURATION) String duration,
      @Size(max = ClinicalFieldLimits.MEDICATION_INDICATIONS) String indication) {
  }
}

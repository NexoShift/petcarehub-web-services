package com.nexoshift.petcarehub.clinicalattention.domain.model.commands;

import com.nexoshift.petcarehub.clinicalattention.domain.model.valueobjects.ClinicalFieldLimits;

import java.util.List;

/** A prescription is issued with all its medications at once (at least one). */
public record RegisterPrescriptionCommand(
    Long consultationId, Long requesterUserId, String generalInstruction,
    List<MedicationItem> medications) {

  /** One medication of the prescription. {@code indication} is optional. */
  public record MedicationItem(String name, String dosage, String frequency, String duration,
                               String indication) {

    public MedicationItem {
      name = CommandValidation.requiredText(
          name, ClinicalFieldLimits.MEDICATION_NAME, "Medication name");
      dosage = CommandValidation.requiredText(
          dosage, ClinicalFieldLimits.MEDICATION_DOSAGE, "Medication dosage");
      frequency = CommandValidation.requiredText(
          frequency, ClinicalFieldLimits.MEDICATION_FREQUENCY, "Medication frequency");
      duration = CommandValidation.requiredText(
          duration, ClinicalFieldLimits.MEDICATION_DURATION, "Medication duration");
      indication = CommandValidation.optionalText(
          indication, ClinicalFieldLimits.MEDICATION_INDICATIONS, "Medication indication");
    }
  }

  public RegisterPrescriptionCommand {
    CommandValidation.requiredId(consultationId, "Consultation id");
    CommandValidation.requiredId(requesterUserId, "Requester user id");
    generalInstruction = CommandValidation.optionalText(generalInstruction,
        ClinicalFieldLimits.PRESCRIPTION_INSTRUCTION, "General instruction");
    if (medications == null || medications.isEmpty()) {
      throw new IllegalArgumentException("A prescription must contain at least one medication");
    }
    if (medications.stream().anyMatch(item -> item == null)) {
      throw new IllegalArgumentException("Medications cannot contain empty items");
    }
    if (medications.size() > ClinicalFieldLimits.MAX_MEDICATIONS) {
      throw new IllegalArgumentException("A prescription cannot have more than "
          + ClinicalFieldLimits.MAX_MEDICATIONS + " medications");
    }
    medications = List.copyOf(medications);
  }
}

package com.nexoshift.petcarehub.clinicalattention.domain.model.commands;

import com.nexoshift.petcarehub.clinicalattention.domain.model.valueobjects.ClinicalFieldLimits;

import java.time.LocalDate;

/** {@code endDate} is optional; when present it cannot be before {@code startDate}. */
public record RegisterTreatmentCommand(
    Long consultationId, Long requesterUserId, String description, String instruction,
    LocalDate startDate, LocalDate endDate) {

  public RegisterTreatmentCommand {
    CommandValidation.requiredId(consultationId, "Consultation id");
    CommandValidation.requiredId(requesterUserId, "Requester user id");
    description = CommandValidation.requiredText(
        description, ClinicalFieldLimits.TREATMENT_DESCRIPTION, "Treatment description");
    instruction = CommandValidation.requiredText(
        instruction, ClinicalFieldLimits.TREATMENT_INSTRUCTION, "Treatment instruction");
    CommandValidation.requiredValue(startDate, "Treatment start date");
    if (endDate != null && endDate.isBefore(startDate)) {
      throw new IllegalArgumentException("Treatment end date cannot be before the start date");
    }
  }
}

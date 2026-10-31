package com.nexoshift.petcarehub.clinicalattention.domain.model.commands;

import com.nexoshift.petcarehub.clinicalattention.domain.model.valueobjects.ClinicalFieldLimits;

public record CancelClinicalConsultationCommand(
    Long consultationId, Long requesterUserId, String reason) {

  public CancelClinicalConsultationCommand {
    CommandValidation.requiredId(consultationId, "Consultation id");
    CommandValidation.requiredId(requesterUserId, "Requester user id");
    reason = CommandValidation.requiredText(
        reason, ClinicalFieldLimits.CANCELLATION_REASON, "Cancellation reason");
  }
}

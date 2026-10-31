package com.nexoshift.petcarehub.clinicalattention.domain.model.commands;

public record StartClinicalConsultationCommand(Long consultationId, Long requesterUserId) {

  public StartClinicalConsultationCommand {
    CommandValidation.requiredId(consultationId, "Consultation id");
    CommandValidation.requiredId(requesterUserId, "Requester user id");
  }
}

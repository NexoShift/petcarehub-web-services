package com.nexoshift.petcarehub.clinicalattention.domain.model.commands;

public record CompleteClinicalConsultationCommand(Long consultationId, Long requesterUserId) {

  public CompleteClinicalConsultationCommand {
    CommandValidation.requiredId(consultationId, "Consultation id");
    CommandValidation.requiredId(requesterUserId, "Requester user id");
  }
}

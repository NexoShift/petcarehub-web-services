package com.nexoshift.petcarehub.clinicalattention.domain.model.commands;

import com.nexoshift.petcarehub.clinicalattention.domain.model.valueobjects.BodySystem;
import com.nexoshift.petcarehub.clinicalattention.domain.model.valueobjects.ClinicalFieldLimits;
import com.nexoshift.petcarehub.clinicalattention.domain.model.valueobjects.FindingSeverity;

public record AddClinicalFindingCommand(
    Long consultationId, Long requesterUserId, BodySystem bodySystem, String description,
    FindingSeverity severity) {

  public AddClinicalFindingCommand {
    CommandValidation.requiredId(consultationId, "Consultation id");
    CommandValidation.requiredId(requesterUserId, "Requester user id");
    CommandValidation.requiredValue(bodySystem, "Body system");
    description = CommandValidation.requiredText(
        description, ClinicalFieldLimits.FINDING_DESCRIPTION, "Finding description");
  }
}

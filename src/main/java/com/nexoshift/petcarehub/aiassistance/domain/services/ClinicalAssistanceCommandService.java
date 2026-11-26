package com.nexoshift.petcarehub.aiassistance.domain.services;

import com.nexoshift.petcarehub.aiassistance.domain.model.aggregates.ClinicalAssistance;
import com.nexoshift.petcarehub.aiassistance.domain.model.commands.DecideOnSoapDraftCommand;
import com.nexoshift.petcarehub.aiassistance.domain.model.commands.RegenerateClinicalAssistanceCommand;
import com.nexoshift.petcarehub.aiassistance.domain.model.commands.RequestClinicalAssistanceCommand;

/** Errors leave as exceptions (400, 403, 404, 409, 503) that the global handler translates. */
public interface ClinicalAssistanceCommandService {

  ClinicalAssistance handle(RequestClinicalAssistanceCommand command);

  ClinicalAssistance handle(RegenerateClinicalAssistanceCommand command);

  ClinicalAssistance handle(DecideOnSoapDraftCommand command);
}

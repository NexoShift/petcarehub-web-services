package com.nexoshift.petcarehub.clinicalattention.interfaces.rest.transform;

import com.nexoshift.petcarehub.clinicalattention.domain.model.commands.ReevaluateDiagnosisCommand;
import com.nexoshift.petcarehub.clinicalattention.interfaces.rest.resources.ReevaluateDiagnosisResource;

public final class ReevaluateDiagnosisCommandFromResourceAssembler {

  private ReevaluateDiagnosisCommandFromResourceAssembler() {
  }

  public static ReevaluateDiagnosisCommand toCommandFromResource(
      Long consultationId, Long previousDiagnosisId, Long requesterUserId,
      ReevaluateDiagnosisResource resource) {
    return new ReevaluateDiagnosisCommand(consultationId, previousDiagnosisId, requesterUserId,
        resource.description(), resource.type(), resource.differentialDiagnoses(),
        resource.rationale());
  }
}

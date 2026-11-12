package com.nexoshift.petcarehub.clinicalattention.interfaces.rest.transform;

import com.nexoshift.petcarehub.clinicalattention.domain.model.commands.RegisterDiagnosisCommand;
import com.nexoshift.petcarehub.clinicalattention.interfaces.rest.resources.RegisterDiagnosisResource;

public final class RegisterDiagnosisCommandFromResourceAssembler {

  private RegisterDiagnosisCommandFromResourceAssembler() {
  }

  public static RegisterDiagnosisCommand toCommandFromResource(
      Long consultationId, Long requesterUserId, RegisterDiagnosisResource resource) {
    return new RegisterDiagnosisCommand(consultationId, requesterUserId, resource.description(),
        resource.type(), resource.primary(), resource.differentialDiagnoses(),
        resource.rationale());
  }
}

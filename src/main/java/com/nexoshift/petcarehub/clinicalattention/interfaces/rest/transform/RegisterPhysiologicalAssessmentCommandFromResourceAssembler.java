package com.nexoshift.petcarehub.clinicalattention.interfaces.rest.transform;

import com.nexoshift.petcarehub.clinicalattention.domain.model.commands.RegisterPhysiologicalAssessmentCommand;
import com.nexoshift.petcarehub.clinicalattention.interfaces.rest.resources.RegisterPhysiologicalAssessmentResource;

public final class RegisterPhysiologicalAssessmentCommandFromResourceAssembler {

  private RegisterPhysiologicalAssessmentCommandFromResourceAssembler() {
  }

  public static RegisterPhysiologicalAssessmentCommand toCommandFromResource(
      Long consultationId, Long requesterUserId,
      RegisterPhysiologicalAssessmentResource resource) {
    return new RegisterPhysiologicalAssessmentCommand(consultationId, requesterUserId,
        resource.weightKg(), resource.temperatureCelsius(), resource.heartRateBpm(),
        resource.respiratoryRateBpm(), resource.bodyConditionScore(), resource.hydrationStatus(),
        resource.mucousMembranes(), resource.capillaryRefillTimeSeconds(),
        resource.observations());
  }
}

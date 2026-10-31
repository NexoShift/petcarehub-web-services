package com.nexoshift.petcarehub.clinicalattention.interfaces.rest.transform;

import com.nexoshift.petcarehub.clinicalattention.domain.model.commands.AddClinicalFindingCommand;
import com.nexoshift.petcarehub.clinicalattention.interfaces.rest.resources.AddClinicalFindingResource;

public final class AddClinicalFindingCommandFromResourceAssembler {

  private AddClinicalFindingCommandFromResourceAssembler() {
  }

  public static AddClinicalFindingCommand toCommandFromResource(
      Long consultationId, Long requesterUserId, AddClinicalFindingResource resource) {
    return new AddClinicalFindingCommand(consultationId, requesterUserId, resource.bodySystem(),
        resource.description(), resource.severity());
  }
}

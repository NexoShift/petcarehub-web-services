package com.nexoshift.petcarehub.clinicalattention.interfaces.rest.transform;

import com.nexoshift.petcarehub.clinicalattention.domain.model.commands.RegisterAnamnesisCommand;
import com.nexoshift.petcarehub.clinicalattention.interfaces.rest.resources.RegisterAnamnesisResource;

public final class RegisterAnamnesisCommandFromResourceAssembler {

  private RegisterAnamnesisCommandFromResourceAssembler() {
  }

  public static RegisterAnamnesisCommand toCommandFromResource(
      Long consultationId, Long requesterUserId, RegisterAnamnesisResource resource) {
    return new RegisterAnamnesisCommand(consultationId, requesterUserId,
        resource.chiefComplaint(), resource.evolutionTime(), resource.symptoms(), resource.diet(),
        resource.behavior(), resource.previousTreatments(), resource.ownerObservations());
  }
}

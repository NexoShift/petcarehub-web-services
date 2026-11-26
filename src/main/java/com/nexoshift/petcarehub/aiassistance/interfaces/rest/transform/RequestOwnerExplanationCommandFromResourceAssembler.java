package com.nexoshift.petcarehub.aiassistance.interfaces.rest.transform;

import com.nexoshift.petcarehub.aiassistance.domain.model.commands.RequestOwnerExplanationCommand;
import com.nexoshift.petcarehub.aiassistance.interfaces.rest.resources.RequestOwnerExplanationResource;

public final class RequestOwnerExplanationCommandFromResourceAssembler {

  private RequestOwnerExplanationCommandFromResourceAssembler() {
  }

  public static RequestOwnerExplanationCommand toCommandFromResource(
      RequestOwnerExplanationResource resource, Long ownerUserId) {
    return new RequestOwnerExplanationCommand(resource.petId(), resource.consultationId(),
        ownerUserId, resource.question());
  }
}

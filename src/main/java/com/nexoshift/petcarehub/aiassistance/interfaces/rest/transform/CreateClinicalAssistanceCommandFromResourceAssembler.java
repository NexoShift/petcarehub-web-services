package com.nexoshift.petcarehub.aiassistance.interfaces.rest.transform;

import com.nexoshift.petcarehub.aiassistance.domain.model.commands.RequestClinicalAssistanceCommand;
import com.nexoshift.petcarehub.aiassistance.domain.model.valueobjects.SelectedBackground;
import com.nexoshift.petcarehub.aiassistance.interfaces.rest.resources.CreateClinicalAssistanceResource;

public final class CreateClinicalAssistanceCommandFromResourceAssembler {

  private CreateClinicalAssistanceCommandFromResourceAssembler() {
  }

  public static RequestClinicalAssistanceCommand toCommandFromResource(
      CreateClinicalAssistanceResource resource, Long veterinarianId) {
    var backgrounds = resource.backgrounds().stream()
        .map(background -> new SelectedBackground(background.type(), background.referenceId()))
        .toList();
    return new RequestClinicalAssistanceCommand(resource.consultationId(), resource.petId(),
        veterinarianId, resource.currentReason(), backgrounds);
  }
}

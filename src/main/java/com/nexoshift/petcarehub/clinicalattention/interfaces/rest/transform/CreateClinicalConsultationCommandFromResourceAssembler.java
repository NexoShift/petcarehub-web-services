package com.nexoshift.petcarehub.clinicalattention.interfaces.rest.transform;

import com.nexoshift.petcarehub.clinicalattention.domain.model.commands.CreateClinicalConsultationCommand;
import com.nexoshift.petcarehub.clinicalattention.interfaces.rest.resources.CreateClinicalConsultationResource;

public final class CreateClinicalConsultationCommandFromResourceAssembler {

  private CreateClinicalConsultationCommandFromResourceAssembler() {
  }

  public static CreateClinicalConsultationCommand toCommandFromResource(
      Long veterinarianUserId, CreateClinicalConsultationResource resource) {
    return new CreateClinicalConsultationCommand(resource.petId(), veterinarianUserId,
        resource.clinicId(), resource.consultingRoomId(), resource.reason());
  }
}

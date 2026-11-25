package com.nexoshift.petcarehub.clinicalattention.interfaces.rest.transform;

import com.nexoshift.petcarehub.clinicalattention.domain.model.entities.Treatment;
import com.nexoshift.petcarehub.clinicalattention.interfaces.rest.resources.TreatmentResource;

public final class TreatmentResourceFromEntityAssembler {

  private TreatmentResourceFromEntityAssembler() {
  }

  public static TreatmentResource toResourceFromEntity(Treatment treatment) {
    return new TreatmentResource(treatment.getId(), treatment.getDescription(),
        treatment.getInstruction(), treatment.getStartDate(), treatment.getEndDate(),
        treatment.getTreatmentStatus(), treatment.getPrescribedBy().veterinarianId(),
        treatment.getCreatedAt());
  }
}

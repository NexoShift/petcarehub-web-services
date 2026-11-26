package com.nexoshift.petcarehub.aiassistance.interfaces.rest.transform;

import com.nexoshift.petcarehub.aiassistance.domain.model.aggregates.OwnerExplanation;
import com.nexoshift.petcarehub.aiassistance.interfaces.rest.resources.OwnerExplanationResource;

public final class OwnerExplanationResourceFromEntityAssembler {

  private OwnerExplanationResourceFromEntityAssembler() {
  }

  public static OwnerExplanationResource toResourceFromEntity(OwnerExplanation entity) {
    return new OwnerExplanationResource(entity.getId(), entity.getPetId(),
        entity.getConsultationId(), entity.getQuestion(), entity.getAnswer(), entity.getModel(),
        entity.getCreatedAt());
  }
}

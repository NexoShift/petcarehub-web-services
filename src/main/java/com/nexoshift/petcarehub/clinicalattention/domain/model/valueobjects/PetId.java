package com.nexoshift.petcarehub.clinicalattention.domain.model.valueobjects;

import jakarta.persistence.Embeddable;

/** Id of a pet. Pets belong to Profiles & Organization, so only the id is kept here. */
@Embeddable
public record PetId(Long petId) {

  public PetId {
    if (petId == null || petId <= 0) {
      throw new IllegalArgumentException("Pet id must be greater than zero");
    }
  }
}

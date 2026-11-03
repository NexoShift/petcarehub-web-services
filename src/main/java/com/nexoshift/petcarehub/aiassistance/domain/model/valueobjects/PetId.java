package com.nexoshift.petcarehub.aiassistance.domain.model.valueobjects;

import jakarta.persistence.Embeddable;

/** Reference to a pet of the Profiles context. */
@Embeddable
public record PetId(Long petId) {

  public PetId {
    if (petId == null || petId <= 0) {
      throw new IllegalArgumentException("Pet id must be greater than zero");
    }
  }
}

package com.nexoshift.petcarehub.medicalhistory.domain.model.valueobjects;

import jakarta.persistence.Embeddable;

/** Pet of Profiles, referenced only by id. */
@Embeddable
public record PetId(Long petId) {

  public PetId {
    if (petId == null || petId <= 0) {
      throw new IllegalArgumentException("Pet id must be greater than zero");
    }
  }
}

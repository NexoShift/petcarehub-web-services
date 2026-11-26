package com.nexoshift.petcarehub.aiassistance.domain.model.valueobjects;

import jakarta.persistence.Embeddable;

/** The veterinarian who requested the assistance. Sprint 1: the IAM user id. */
@Embeddable
public record VeterinarianId(Long veterinarianId) {

  public VeterinarianId {
    if (veterinarianId == null || veterinarianId <= 0) {
      throw new IllegalArgumentException("Veterinarian id must be greater than zero");
    }
  }
}

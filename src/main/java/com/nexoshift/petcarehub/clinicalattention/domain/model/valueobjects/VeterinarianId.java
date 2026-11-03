package com.nexoshift.petcarehub.clinicalattention.domain.model.valueobjects;

import jakarta.persistence.Embeddable;

/** Id of a veterinarian. Veterinarians belong to Profiles & Organization. */
@Embeddable
public record VeterinarianId(Long veterinarianId) {

  public VeterinarianId {
    if (veterinarianId == null || veterinarianId <= 0) {
      throw new IllegalArgumentException("Veterinarian id must be greater than zero");
    }
  }
}

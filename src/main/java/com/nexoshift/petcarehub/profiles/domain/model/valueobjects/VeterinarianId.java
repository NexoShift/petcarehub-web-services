package com.nexoshift.petcarehub.profiles.domain.model.valueobjects;

public record VeterinarianId(Long veterinarianId) {

  public VeterinarianId {
    if (veterinarianId == null || veterinarianId <= 0) {
      throw new IllegalArgumentException("Veterinarian ID must be greater than zero");
    }
  }
}

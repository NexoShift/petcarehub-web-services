package com.nexoshift.petcarehub.profiles.domain.model.valueobjects;

public record ClinicId(Long clinicId) {

  public ClinicId {
    if (clinicId == null || clinicId <= 0) {
      throw new IllegalArgumentException("Clinic ID must be greater than zero");
    }
  }
}

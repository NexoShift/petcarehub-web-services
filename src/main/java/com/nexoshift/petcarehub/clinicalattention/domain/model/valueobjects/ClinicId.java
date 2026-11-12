package com.nexoshift.petcarehub.clinicalattention.domain.model.valueobjects;

import jakarta.persistence.Embeddable;

/** Id of a clinic. Clinics belong to Profiles & Organization. */
@Embeddable
public record ClinicId(Long clinicId) {

  public ClinicId {
    if (clinicId == null || clinicId <= 0) {
      throw new IllegalArgumentException("Clinic id must be greater than zero");
    }
  }
}

package com.nexoshift.petcarehub.clinicalattention.domain.model.queries;

/** Consultations attended by a veterinarian, newest first. */
public record GetClinicalConsultationsByVeterinarianIdQuery(Long veterinarianId) {

  public GetClinicalConsultationsByVeterinarianIdQuery {
    if (veterinarianId == null || veterinarianId <= 0) {
      throw new IllegalArgumentException("Veterinarian id must be greater than zero");
    }
  }
}

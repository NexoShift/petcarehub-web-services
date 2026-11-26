package com.nexoshift.petcarehub.clinicalattention.domain.model.queries;

/**
 * Completed consultations of a pet, newest first. It does not check permissions: it is only for
 * the published facade, whose callers already checked them.
 */
public record GetCompletedClinicalConsultationsByPetIdQuery(Long petId) {

  public GetCompletedClinicalConsultationsByPetIdQuery {
    if (petId == null || petId <= 0) {
      throw new IllegalArgumentException("Pet id must be greater than zero");
    }
  }
}

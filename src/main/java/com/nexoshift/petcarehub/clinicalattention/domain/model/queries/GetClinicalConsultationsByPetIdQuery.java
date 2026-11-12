package com.nexoshift.petcarehub.clinicalattention.domain.model.queries;

/** The requester must be the owner of the pet or a veterinarian of a clinic where the pet is a patient. */
public record GetClinicalConsultationsByPetIdQuery(Long petId, Long requesterUserId) {

  public GetClinicalConsultationsByPetIdQuery {
    if (petId == null || petId <= 0) {
      throw new IllegalArgumentException("Pet id must be greater than zero");
    }
    if (requesterUserId == null || requesterUserId <= 0) {
      throw new IllegalArgumentException("Requester user id must be greater than zero");
    }
  }
}

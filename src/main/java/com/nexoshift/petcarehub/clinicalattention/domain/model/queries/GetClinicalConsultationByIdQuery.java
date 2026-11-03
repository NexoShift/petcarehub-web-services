package com.nexoshift.petcarehub.clinicalattention.domain.model.queries;

public record GetClinicalConsultationByIdQuery(Long consultationId) {

  public GetClinicalConsultationByIdQuery {
    if (consultationId == null || consultationId <= 0) {
      throw new IllegalArgumentException("Consultation id must be greater than zero");
    }
  }
}

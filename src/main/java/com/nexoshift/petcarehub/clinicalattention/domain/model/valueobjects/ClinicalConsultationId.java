package com.nexoshift.petcarehub.clinicalattention.domain.model.valueobjects;

import jakarta.persistence.Embeddable;

/** Id of a clinical consultation, used by Treatment and Prescription to refer to it. */
@Embeddable
public record ClinicalConsultationId(Long clinicalConsultationId) {

  public ClinicalConsultationId {
    if (clinicalConsultationId == null || clinicalConsultationId <= 0) {
      throw new IllegalArgumentException("Clinical consultation id must be greater than zero");
    }
  }
}

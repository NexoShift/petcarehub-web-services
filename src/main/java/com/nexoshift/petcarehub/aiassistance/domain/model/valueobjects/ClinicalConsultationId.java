package com.nexoshift.petcarehub.aiassistance.domain.model.valueobjects;

import jakarta.persistence.Embeddable;

/** Reference to a consultation of the Clinical Attention context. */
@Embeddable
public record ClinicalConsultationId(Long consultationId) {

  public ClinicalConsultationId {
    if (consultationId == null || consultationId <= 0) {
      throw new IllegalArgumentException("Consultation id must be greater than zero");
    }
  }
}

package com.nexoshift.petcarehub.clinicalexams.domain.model.valueobjects;

import jakarta.persistence.Embeddable;

@Embeddable
public record ClinicalConsultationId(Long clinicalConsultationId) {

    public ClinicalConsultationId {
        if (clinicalConsultationId == null || clinicalConsultationId <= 0) {
            throw new IllegalArgumentException("Clinical consultation id must be greater than zero");
        }
    }
}
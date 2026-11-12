package com.nexoshift.petcarehub.clinicalexams.domain.model.valueobjects;

import jakarta.persistence.Embeddable;

/** Clinic that owns the exam (Profiles). It lets the clinic list its pending exams. */
@Embeddable
public record ClinicId(Long clinicId) {

    public ClinicId {
        if (clinicId == null || clinicId <= 0) {
            throw new IllegalArgumentException("Clinic id must be greater than zero");
        }
    }
}

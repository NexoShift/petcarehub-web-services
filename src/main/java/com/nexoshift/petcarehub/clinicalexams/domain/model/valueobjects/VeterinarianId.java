package com.nexoshift.petcarehub.clinicalexams.domain.model.valueobjects;

import jakarta.persistence.Embeddable;

@Embeddable
public record VeterinarianId(Long veterinarianId) {

    public VeterinarianId {
        if (veterinarianId == null || veterinarianId <= 0) {
            throw new IllegalArgumentException("Veterinarian id must be greater than zero");
        }
    }
}
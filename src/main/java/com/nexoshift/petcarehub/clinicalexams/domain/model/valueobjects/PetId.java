package com.nexoshift.petcarehub.clinicalexams.domain.model.valueobjects;

import jakarta.persistence.Embeddable;

@Embeddable
public record PetId(Long petId) {

    public PetId {
        if (petId == null || petId <= 0) {
            throw new IllegalArgumentException("Pet id must be greater than zero");
        }
    }
}
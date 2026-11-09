package com.nexoshift.petcarehub.profiles.domain.model.valueobjects;

import jakarta.persistence.Embeddable;

@Embeddable
public record PetId(Long petId) {

    public PetId {
        if (petId == null || petId <= 0) {
            throw new IllegalArgumentException(
                    "Pet ID must be a positive number");
        }
    }
}

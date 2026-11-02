package com.nexoshift.petcarehub.profiles.domain.model.queries;

/**
 * Query to retrieve a pet by its profile identifier.
 *
 * @param petId the pet profile identifier
 */
public record GetPetByIdQuery(Long petId) {

    public GetPetByIdQuery {
        if (petId == null || petId <= 0) {
            throw new IllegalArgumentException(
                    "Pet ID must be a positive number");
        }
    }
}
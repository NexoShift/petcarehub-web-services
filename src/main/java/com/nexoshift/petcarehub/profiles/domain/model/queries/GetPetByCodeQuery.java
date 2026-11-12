package com.nexoshift.petcarehub.profiles.domain.model.queries;

/**
 * Query to retrieve a pet by its unique identification code.
 *
 * @param petCode the pet identification code
 */
public record GetPetByCodeQuery(String petCode) {

    public GetPetByCodeQuery {
        if (petCode == null || petCode.isBlank()) {
            throw new IllegalArgumentException("Pet code is required");
        }
    }
}

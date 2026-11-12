package com.nexoshift.petcarehub.profiles.domain.model.queries;

/**
 * Query to retrieve a pet owner by its profile identifier.
 *
 * @param petOwnerId the pet owner profile identifier
 */
public record GetPetOwnerByIdQuery(Long petOwnerId) {

    public GetPetOwnerByIdQuery {
        if (petOwnerId == null || petOwnerId <= 0) {
            throw new IllegalArgumentException(
                    "Pet owner ID must be a positive number");
        }
    }
}

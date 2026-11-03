package com.nexoshift.petcarehub.profiles.domain.model.queries;

/**
 * Query to retrieve all pets belonging to a pet owner.
 *
 * @param ownerId the pet owner profile identifier
 */
public record GetPetsByOwnerIdQuery(Long ownerId) {

    public GetPetsByOwnerIdQuery {
        if (ownerId == null || ownerId <= 0) {
            throw new IllegalArgumentException(
                    "Owner ID must be a positive number");
        }
    }
}
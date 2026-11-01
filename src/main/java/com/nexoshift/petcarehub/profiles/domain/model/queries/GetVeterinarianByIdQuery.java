package com.nexoshift.petcarehub.profiles.domain.model.queries;

/**
 * Query to retrieve a veterinarian by its profile identifier.
 *
 * @param veterinarianId the veterinarian profile identifier
 */
public record GetVeterinarianByIdQuery(Long veterinarianId) {

    public GetVeterinarianByIdQuery {
        if (veterinarianId == null || veterinarianId <= 0) {
            throw new IllegalArgumentException(
                    "Veterinarian ID must be a positive number");
        }
    }
}

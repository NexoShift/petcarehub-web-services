package com.nexoshift.petcarehub.profiles.domain.model.queries;

/**
 * Query to retrieve clinics where a pet is registered as a patient.
 *
 * @param petId the pet identifier
 */
public record GetPatientClinicsByPetIdQuery(Long petId) {

    public GetPatientClinicsByPetIdQuery {
        if (petId == null || petId <= 0) {
            throw new IllegalArgumentException(
                    "Pet ID must be a positive number");
        }
    }
}

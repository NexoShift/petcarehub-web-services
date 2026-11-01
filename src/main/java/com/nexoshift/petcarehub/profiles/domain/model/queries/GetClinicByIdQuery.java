package com.nexoshift.petcarehub.profiles.domain.model.queries;

/**
 * Query to retrieve a clinic by its profile identifier.
 *
 * @param clinicId the clinic profile identifier
 */
public record GetClinicByIdQuery(Long clinicId) {

    public GetClinicByIdQuery {
        if (clinicId == null || clinicId <= 0) {
            throw new IllegalArgumentException(
                    "Clinic ID must be a positive number");
        }
    }
}
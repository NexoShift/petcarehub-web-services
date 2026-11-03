package com.nexoshift.petcarehub.profiles.domain.model.queries;

/**
 * Query to retrieve pets registered as patients of a clinic.
 *
 * @param clinicId the clinic identifier
 */
public record GetPatientPetsByClinicIdQuery(Long clinicId) {

    public GetPatientPetsByClinicIdQuery {
        if (clinicId == null || clinicId <= 0) {
            throw new IllegalArgumentException(
                    "Clinic ID must be a positive number");
        }
    }
}

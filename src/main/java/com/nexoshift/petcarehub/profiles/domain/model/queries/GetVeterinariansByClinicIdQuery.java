package com.nexoshift.petcarehub.profiles.domain.model.queries;

/**
 * Query to retrieve all veterinarians belonging to a clinic.
 *
 * @param clinicId the clinic profile identifier
 */
public record GetVeterinariansByClinicIdQuery(Long clinicId) {

    public GetVeterinariansByClinicIdQuery {
        if (clinicId == null || clinicId <= 0) {
            throw new IllegalArgumentException(
                    "Clinic ID must be a positive number");
        }
    }
}

package com.nexoshift.petcarehub.profiles.domain.model.queries;

/**
 * Query to retrieve consulting room assignment history for a clinic.
 *
 * @param clinicId the clinic profile identifier
 */
public record GetConsultingRoomAssignmentsByClinicIdQuery(Long clinicId) {

    public GetConsultingRoomAssignmentsByClinicIdQuery {
        if (clinicId == null || clinicId <= 0) {
            throw new IllegalArgumentException(
                    "Clinic ID must be a positive number");
        }
    }
}

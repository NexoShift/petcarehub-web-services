package com.nexoshift.petcarehub.profiles.domain.model.queries;

/**
 * Query to retrieve all consulting rooms belonging to a clinic.
 *
 * @param clinicId the clinic profile identifier
 */
public record GetConsultingRoomsByClinicIdQuery(Long clinicId) {

    public GetConsultingRoomsByClinicIdQuery {
        if (clinicId == null || clinicId <= 0) {
            throw new IllegalArgumentException(
                    "Clinic ID must be a positive number");
        }
    }
}

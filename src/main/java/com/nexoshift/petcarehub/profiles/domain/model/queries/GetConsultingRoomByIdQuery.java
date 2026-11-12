package com.nexoshift.petcarehub.profiles.domain.model.queries;

/**
 * Query to retrieve a consulting room belonging to a clinic.
 *
 * @param clinicId the clinic profile identifier
 * @param consultingRoomId the consulting room identifier
 */
public record GetConsultingRoomByIdQuery(
        Long clinicId,
        Long consultingRoomId
) {
    public GetConsultingRoomByIdQuery {
        if (clinicId == null || clinicId <= 0) {
            throw new IllegalArgumentException(
                    "Clinic ID must be a positive number");
        }

        if (consultingRoomId == null || consultingRoomId <= 0) {
            throw new IllegalArgumentException(
                    "Consulting room ID must be a positive number");
        }
    }
}

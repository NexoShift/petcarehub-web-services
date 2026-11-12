package com.nexoshift.petcarehub.profiles.domain.model.commands;

/**
 * Command to deactivate a consulting room.
 *
 * @param clinicId the clinic profile identifier
 * @param consultingRoomId the consulting room identifier
 */
public record DeactivateConsultingRoomCommand(
        Long clinicId,
        Long consultingRoomId
) {
    public DeactivateConsultingRoomCommand {
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
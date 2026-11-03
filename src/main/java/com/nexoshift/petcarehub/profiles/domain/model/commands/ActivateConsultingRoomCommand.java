package com.nexoshift.petcarehub.profiles.domain.model.commands;

/**
 * Command to activate a consulting room.
 *
 * @param clinicId the clinic profile identifier
 * @param consultingRoomId the consulting room identifier
 */
public record ActivateConsultingRoomCommand(
        Long clinicId,
        Long consultingRoomId
) {
    public ActivateConsultingRoomCommand {
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

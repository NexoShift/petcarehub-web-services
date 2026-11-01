package com.nexoshift.petcarehub.profiles.domain.model.commands;

/**
 * Command to update a consulting room.
 *
 * @param clinicId the clinic profile identifier
 * @param consultingRoomId the consulting room identifier
 * @param name the consulting room name
 * @param description the consulting room description
 */
public record UpdateConsultingRoomCommand(
        Long clinicId,
        Long consultingRoomId,
        String name,
        String description
) {
    public UpdateConsultingRoomCommand {
        if (clinicId == null || clinicId <= 0) {
            throw new IllegalArgumentException(
                    "Clinic ID must be a positive number");
        }

        if (consultingRoomId == null || consultingRoomId <= 0) {
            throw new IllegalArgumentException(
                    "Consulting room ID must be a positive number");
        }

        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException(
                    "Consulting room name is required");
        }

        name = name.trim();
    }
}

package com.nexoshift.petcarehub.profiles.domain.model.commands;

/**
 * Command to create a consulting room within a clinic.
 *
 * @param clinicId the clinic profile identifier
 * @param name the consulting room name
 * @param description the consulting room description
 */
public record CreateConsultingRoomCommand(
        Long clinicId,
        String name,
        String description
) {
    public CreateConsultingRoomCommand {
        if (clinicId == null || clinicId <= 0) {
            throw new IllegalArgumentException(
                    "Clinic ID must be a positive number");
        }

        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException(
                    "Consulting room name is required");
        }

        name = name.trim();
    }
}

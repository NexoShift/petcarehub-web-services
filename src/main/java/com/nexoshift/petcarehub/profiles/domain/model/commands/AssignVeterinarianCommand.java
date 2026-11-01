package com.nexoshift.petcarehub.profiles.domain.model.commands;

/**
 * Command to assign a veterinarian to a consulting room.
 * An existing active assignment is automatically replaced.
 *
 * @param clinicId the clinic profile identifier
 * @param consultingRoomId the consulting room identifier
 * @param veterinarianId the veterinarian profile identifier
 */
public record AssignVeterinarianCommand(
        Long clinicId,
        Long consultingRoomId,
        Long veterinarianId
) {
    public AssignVeterinarianCommand {
        if (clinicId == null || clinicId <= 0) {
            throw new IllegalArgumentException(
                    "Clinic ID must be a positive number");
        }

        if (consultingRoomId == null || consultingRoomId <= 0) {
            throw new IllegalArgumentException(
                    "Consulting room ID must be a positive number");
        }

        if (veterinarianId == null || veterinarianId <= 0) {
            throw new IllegalArgumentException(
                    "Veterinarian ID must be a positive number");
        }
    }
}

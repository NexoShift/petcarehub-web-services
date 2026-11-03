package com.nexoshift.petcarehub.profiles.domain.model.commands;

/**
 * Command to end an active consulting room assignment.
 *
 * @param clinicId the clinic profile identifier
 * @param assignmentId the consulting room assignment identifier
 */
public record UnassignVeterinarianCommand(
        Long clinicId,
        Long assignmentId
) {
    public UnassignVeterinarianCommand {
        if (clinicId == null || clinicId <= 0) {
            throw new IllegalArgumentException(
                    "Clinic ID must be a positive number");
        }

        if (assignmentId == null || assignmentId <= 0) {
            throw new IllegalArgumentException(
                    "Assignment ID must be a positive number");
        }
    }
}
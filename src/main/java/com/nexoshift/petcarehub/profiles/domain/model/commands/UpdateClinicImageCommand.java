package com.nexoshift.petcarehub.profiles.domain.model.commands;

/**
 * Command to update a clinic's profile image.
 *
 * @param clinicId the clinic profile identifier
 * @param profileImageKey the S3 object key of the new profile image
 */
public record UpdateClinicImageCommand(
        Long clinicId,
        String profileImageKey
) {
    public UpdateClinicImageCommand {
        if (clinicId == null || clinicId <= 0) {
            throw new IllegalArgumentException("Clinic ID must be a positive number");
        }

        if (profileImageKey == null || profileImageKey.isBlank()) {
            throw new IllegalArgumentException("Profile image key is required");
        }

        profileImageKey = profileImageKey.trim();
    }
}

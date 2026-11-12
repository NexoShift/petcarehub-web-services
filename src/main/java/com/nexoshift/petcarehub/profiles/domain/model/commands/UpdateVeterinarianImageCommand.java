package com.nexoshift.petcarehub.profiles.domain.model.commands;

/**
 * Command to update a veterinarian's profile image.
 *
 * @param veterinarianId the veterinarian profile identifier
 * @param profileImageKey the S3 object key of the new profile image
 */
public record UpdateVeterinarianImageCommand(
        Long veterinarianId,
        String profileImageKey
) {
    public UpdateVeterinarianImageCommand {
        if (veterinarianId == null || veterinarianId <= 0) {
            throw new IllegalArgumentException(
                    "Veterinarian ID must be a positive number");
        }

        if (profileImageKey == null || profileImageKey.isBlank()) {
            throw new IllegalArgumentException(
                    "Profile image key is required");
        }

        profileImageKey = profileImageKey.trim();
    }
}

package com.nexoshift.petcarehub.profiles.domain.model.commands;

/**
 * Command to update a pet's profile image.
 *
 * @param petId the pet profile identifier
 * @param profileImageKey the S3 object key of the new profile image
 */
public record UpdatePetImageCommand(
        Long petId,
        String profileImageKey
) {
    public UpdatePetImageCommand {
        if (petId == null || petId <= 0) {
            throw new IllegalArgumentException("Pet ID must be a positive number");
        }

        if (profileImageKey == null || profileImageKey.isBlank()) {
            throw new IllegalArgumentException("Profile image key is required");
        }

        profileImageKey = profileImageKey.trim();
    }
}

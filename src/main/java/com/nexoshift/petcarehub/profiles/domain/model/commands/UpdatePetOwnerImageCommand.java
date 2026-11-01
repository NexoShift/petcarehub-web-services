package com.nexoshift.petcarehub.profiles.domain.model.commands;

/**
 * Command to update a pet owner's profile image.
 *
 * @param petOwnerId the pet owner profile identifier
 * @param profileImageKey the S3 object key of the new profile image
 */
public record UpdatePetOwnerImageCommand(
        Long petOwnerId,
        String profileImageKey
) {
    public UpdatePetOwnerImageCommand {
        if (petOwnerId == null || petOwnerId <= 0) {
            throw new IllegalArgumentException("Pet owner ID must be a positive number");
        }

        if (profileImageKey == null || profileImageKey.isBlank()) {
            throw new IllegalArgumentException("Profile image key is required");
        }

        profileImageKey = profileImageKey.trim();
    }
}

package com.nexoshift.petcarehub.profiles.domain.model.commands;

import com.nexoshift.petcarehub.profiles.domain.model.valueobjects.PersonSex;

/**
 * Command to create a pet owner profile.
 *
 * @param userId the IAM user identifier
 * @param firstName the pet owner's first name
 * @param lastName the pet owner's last name
 * @param email the registered email address
 * @param phoneNumber the contact phone number
 * @param sex the person's sex
 * @param profileImageKey the S3 object key of the profile image
 */
public record CreatePetOwnerCommand(Long userId, String firstName, String lastName, String email,
        String phoneNumber, PersonSex sex, String profileImageKey) {

    public CreatePetOwnerCommand {
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException(
                    "User ID must be a positive number");
        }

        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException(
                    "Email is required");
        }

        email = email.trim();
    }
}

package com.nexoshift.petcarehub.profiles.domain.model.commands;

import com.nexoshift.petcarehub.profiles.domain.model.valueobjects.PersonSex;

/**
 * Command to create a veterinarian profile.
 *
 * @param userId the IAM user identifier
 * @param clinicId the clinic profile identifier
 * @param firstName the first name
 * @param lastName the last name
 * @param email the registered email address
 * @param phoneNumber the contact phone number
 * @param sex the person's sex
 * @param licenseNumber the professional license number
 * @param specialty the veterinarian's specialty
 * @param profileImageKey the S3 object key of the profile image
 */
public record CreateVeterinarianCommand(
        Long userId,
        Long clinicId,
        String firstName,
        String lastName,
        String email,
        String phoneNumber,
        PersonSex sex,
        String licenseNumber,
        String specialty,
        String profileImageKey
) {
    public CreateVeterinarianCommand {
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException(
                    "User ID must be a positive number");
        }

        if (clinicId == null || clinicId <= 0) {
            throw new IllegalArgumentException(
                    "Clinic ID must be a positive number");
        }

        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException(
                    "Email is required");
        }

        email = email.trim();
    }
}

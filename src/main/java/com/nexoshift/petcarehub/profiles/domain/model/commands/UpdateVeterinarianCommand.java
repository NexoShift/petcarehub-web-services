package com.nexoshift.petcarehub.profiles.domain.model.commands;

import com.nexoshift.petcarehub.profiles.domain.model.valueobjects.PersonSex;

/**
 * Command to update a veterinarian's professional profile.
 *
 * @param veterinarianId the veterinarian profile identifier
 * @param firstName the first name
 * @param lastName the last name
 * @param phoneNumber the contact phone number
 * @param sex the person's sex
 * @param licenseNumber the professional license number
 * @param specialty the veterinarian's specialty
 */
public record UpdateVeterinarianCommand(
        Long veterinarianId,
        String firstName,
        String lastName,
        String phoneNumber,
        PersonSex sex,
        String licenseNumber,
        String specialty
) {
    public UpdateVeterinarianCommand {
        if (veterinarianId == null || veterinarianId <= 0) {
            throw new IllegalArgumentException(
                    "Veterinarian ID must be a positive number");
        }
    }
}

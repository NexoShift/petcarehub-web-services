package com.nexoshift.petcarehub.profiles.domain.model.commands;

import com.nexoshift.petcarehub.profiles.domain.model.valueobjects.PersonSex;

/**
 * Command to update a pet owner's personal information.
 *
 * @param petOwnerId the pet owner profile identifier
 * @param firstName the first name
 * @param lastName the last name
 * @param phoneNumber the contact phone number
 * @param sex the person's sex
 */
public record UpdatePetOwnerCommand(
        Long petOwnerId,
        String firstName,
        String lastName,
        String phoneNumber,
        PersonSex sex
) {
    public UpdatePetOwnerCommand {
        if (petOwnerId == null || petOwnerId <= 0) {
            throw new IllegalArgumentException("Pet owner ID must be a positive number");
        }
    }
}

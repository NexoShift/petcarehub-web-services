package com.nexoshift.petcarehub.profiles.interfaces.rest.resources;

import com.nexoshift.petcarehub.profiles.domain.model.valueobjects.PersonSex;

/**
 * Resource containing the information required to update
 * a pet owner's personal profile.
 *
 * @param firstName the first name
 * @param lastName the last name
 * @param phoneNumber the contact phone number
 * @param sex the person's sex
 */
public record UpdatePetOwnerResource(
        String firstName,
        String lastName,
        String phoneNumber,
        PersonSex sex
) {
}

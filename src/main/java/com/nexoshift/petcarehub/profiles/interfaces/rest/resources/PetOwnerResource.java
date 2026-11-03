package com.nexoshift.petcarehub.profiles.interfaces.rest.resources;

import com.nexoshift.petcarehub.profiles.domain.model.valueobjects.PersonSex;

/**
 * Resource representing a pet owner profile.
 *
 * @param id the pet owner profile identifier
 * @param userId the IAM user identifier
 * @param firstName the first name
 * @param lastName the last name
 * @param email the registered email address
 * @param phoneNumber the contact phone number
 * @param sex the person's sex
 * @param profileImageUrl the URL of the profile image
 */
public record PetOwnerResource(
        Long id,
        Long userId,
        String firstName,
        String lastName,
        String email,
        String phoneNumber,
        PersonSex sex,
        String profileImageUrl
) {
}

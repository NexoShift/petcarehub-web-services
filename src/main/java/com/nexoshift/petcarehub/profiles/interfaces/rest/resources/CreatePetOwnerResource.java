package com.nexoshift.petcarehub.profiles.interfaces.rest.resources;

import com.nexoshift.petcarehub.profiles.domain.model.valueobjects.PersonSex;
import org.springframework.web.multipart.MultipartFile;

/**
 * Resource containing the information required to create
 * a pet owner profile.
 *
 * @param userId the IAM user identifier
 * @param firstName the first name
 * @param lastName the last name
 * @param email the registered email address
 * @param phoneNumber the contact phone number
 * @param sex the person's sex
 */
public record CreatePetOwnerResource(
        Long userId,
        String firstName,
        String lastName,
        String email,
        String phoneNumber,
        PersonSex sex,
        MultipartFile image
) {
}

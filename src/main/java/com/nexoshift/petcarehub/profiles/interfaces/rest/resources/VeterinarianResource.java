package com.nexoshift.petcarehub.profiles.interfaces.rest.resources;

import com.nexoshift.petcarehub.profiles.domain.model.valueobjects.PersonSex;

/**
 * Resource representing a veterinarian profile.
 */
public record VeterinarianResource(
        Long id,
        Long userId,
        Long clinicId,
        String firstName,
        String lastName,
        String email,
        String phoneNumber,
        PersonSex sex,
        String licenseNumber,
        String specialty,
        String profileImageUrl
) {
}

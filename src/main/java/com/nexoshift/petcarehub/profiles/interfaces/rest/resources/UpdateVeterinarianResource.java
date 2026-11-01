package com.nexoshift.petcarehub.profiles.interfaces.rest.resources;

import com.nexoshift.petcarehub.profiles.domain.model.valueobjects.PersonSex;

/**
 * Resource containing the information required to update a veterinarian.
 */
public record UpdateVeterinarianResource(
        String firstName,
        String lastName,
        String phoneNumber,
        PersonSex sex,
        String licenseNumber,
        String specialty
) {
}

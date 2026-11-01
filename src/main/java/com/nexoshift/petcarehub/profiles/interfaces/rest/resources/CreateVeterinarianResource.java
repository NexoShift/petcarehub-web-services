package com.nexoshift.petcarehub.profiles.interfaces.rest.resources;

import com.nexoshift.petcarehub.profiles.domain.model.valueobjects.PersonSex;
import org.springframework.web.multipart.MultipartFile;

/**
 * Resource containing the information required to create a veterinarian.
 */
public record CreateVeterinarianResource(
        Long userId,
        Long clinicId,
        String firstName,
        String lastName,
        String email,
        String phoneNumber,
        PersonSex sex,
        String licenseNumber,
        String specialty,
        MultipartFile image
) {
}

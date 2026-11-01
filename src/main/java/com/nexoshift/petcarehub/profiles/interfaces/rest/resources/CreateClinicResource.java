package com.nexoshift.petcarehub.profiles.interfaces.rest.resources;

import org.springframework.web.multipart.MultipartFile;

/**
 * Resource containing the information required to create a clinic.
 */
public record CreateClinicResource(
        Long accountId,
        String name,
        String ruc,
        String address,
        String phoneNumber,
        String description,
        MultipartFile image
) {
}

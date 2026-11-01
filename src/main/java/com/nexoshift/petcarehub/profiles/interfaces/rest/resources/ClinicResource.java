package com.nexoshift.petcarehub.profiles.interfaces.rest.resources;

/**
 * Resource representing a clinic profile.
 */
public record ClinicResource(
        Long id,
        Long accountId,
        String name,
        String ruc,
        String address,
        String phoneNumber,
        String description,
        String profileImageUrl
) {
}

package com.nexoshift.petcarehub.profiles.interfaces.rest.resources;

/**
 * Resource containing the information required to update a clinic.
 */
public record UpdateClinicResource(
        String name,
        String ruc,
        String address,
        String phoneNumber,
        String description
) {
}

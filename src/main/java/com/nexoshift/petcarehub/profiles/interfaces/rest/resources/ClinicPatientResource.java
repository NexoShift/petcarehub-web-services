package com.nexoshift.petcarehub.profiles.interfaces.rest.resources;

import java.time.Instant;

/**
 * Resource representing a clinic patient registration.
 *
 * @param id the clinic patient identifier
 * @param clinicId the clinic identifier
 * @param petId the pet identifier
 * @param registeredAt the registration timestamp
 */
public record ClinicPatientResource(
        Long id,
        Long clinicId,
        Long petId,
        Instant registeredAt
) {
}

package com.nexoshift.petcarehub.profiles.interfaces.rest.resources;

/**
 * Resource for registering a pet as a clinic patient.
 *
 * @param petId the pet identifier
 */
public record CreateClinicPatientResource(Long petId) {
}

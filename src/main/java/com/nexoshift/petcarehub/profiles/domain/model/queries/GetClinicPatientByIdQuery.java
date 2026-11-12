package com.nexoshift.petcarehub.profiles.domain.model.queries;

/**
 * Query to retrieve a clinic patient by its identifier.
 *
 * @param clinicPatientId the clinic patient identifier
 */
public record GetClinicPatientByIdQuery(Long clinicPatientId) {
}

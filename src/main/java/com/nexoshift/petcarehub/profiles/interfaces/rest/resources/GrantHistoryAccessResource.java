package com.nexoshift.petcarehub.profiles.interfaces.rest.resources;

/**
 * The owner gives a clinic access to the records of other clinics.
 *
 * @param clinicId a clinic where the pet is a patient
 * @param hours how long the access lasts (1 to 72)
 */
public record GrantHistoryAccessResource(Long clinicId, Integer hours) {
}

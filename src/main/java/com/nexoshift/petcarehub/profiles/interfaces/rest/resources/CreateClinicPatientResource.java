package com.nexoshift.petcarehub.profiles.interfaces.rest.resources;

/**
 * Resource for registering a pet as a clinic patient.
 * <p>
 * The pet code (PET-XXXXXXXX) is the owner's consent: only the owner sees it, and
 * shares it with the clinic. Without the right code the clinic cannot register the pet,
 * so it cannot read the pet's medical history either.
 * </p>
 *
 * @param petId the pet identifier
 * @param petCode the pet code shared by the owner
 */
public record CreateClinicPatientResource(Long petId, String petCode) {
}

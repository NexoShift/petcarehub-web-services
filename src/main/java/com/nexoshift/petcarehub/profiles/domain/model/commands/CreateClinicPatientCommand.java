package com.nexoshift.petcarehub.profiles.domain.model.commands;

/**
 * Command to register a pet as a clinic patient.
 *
 * @param clinicId the clinic identifier
 * @param petId the pet identifier
 * @param petCode the pet code the owner shared with the clinic (the owner's consent)
 */
public record CreateClinicPatientCommand(
        Long clinicId,
        Long petId,
        String petCode
) {
    public CreateClinicPatientCommand {
        if (clinicId == null || clinicId <= 0) {
            throw new IllegalArgumentException(
                    "Clinic ID must be a positive number");
        }

        if (petId == null || petId <= 0) {
            throw new IllegalArgumentException(
                    "Pet ID must be a positive number");
        }

        if (petCode == null || petCode.isBlank()) {
            throw new IllegalArgumentException(
                    "Pet code is required: ask the owner for the pet's code");
        }
    }
}

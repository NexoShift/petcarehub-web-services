package com.nexoshift.petcarehub.profiles.domain.model.commands;

/**
 * Command to register a pet as a clinic patient.
 *
 * @param clinicId the clinic identifier
 * @param petId the pet identifier
 */
public record CreateClinicPatientCommand(
        Long clinicId,
        Long petId
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
    }
}

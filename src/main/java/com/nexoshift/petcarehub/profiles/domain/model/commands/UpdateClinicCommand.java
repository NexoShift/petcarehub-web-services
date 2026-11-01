package com.nexoshift.petcarehub.profiles.domain.model.commands;

/**
 * Command to update a clinic's institutional information.
 *
 * @param clinicId the clinic profile identifier
 * @param name the clinic name
 * @param ruc the clinic tax identification number
 * @param address the clinic address
 * @param phoneNumber the contact phone number
 * @param description the clinic description
 */
public record UpdateClinicCommand(
        Long clinicId,
        String name,
        String ruc,
        String address,
        String phoneNumber,
        String description
) {
    public UpdateClinicCommand {
        if (clinicId == null || clinicId <= 0) {
            throw new IllegalArgumentException("Clinic ID must be a positive number");
        }
    }
}

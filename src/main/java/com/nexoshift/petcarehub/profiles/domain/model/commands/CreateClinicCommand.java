package com.nexoshift.petcarehub.profiles.domain.model.commands;

/**
 * Command to create a clinic profile.
 *
 * @param accountId the IAM account identifier
 * @param name the clinic name
 * @param ruc the clinic tax identification number
 * @param address the clinic address
 * @param phoneNumber the contact phone number
 * @param description the clinic description
 * @param profileImageKey the S3 object key of the profile image
 */
public record CreateClinicCommand(Long accountId, String name, String ruc, String address,
        String phoneNumber, String description, String profileImageKey) {

    public CreateClinicCommand {
        if (accountId == null || accountId <= 0) {
            throw new IllegalArgumentException(
                    "Account ID must be a positive number");
        }

        if (ruc != null && !ruc.matches("\\d{11}")) {
            throw new IllegalArgumentException(
                    "RUC must contain exactly 11 digits");
        }
    }
}

package com.nexoshift.petcarehub.profiles.domain.model.commands;

/**
 * The owner lets a clinic see the records that other clinics wrote about the pet,
 * for a limited time.
 *
 * @param petId the pet identifier
 * @param clinicId the clinic that receives the access
 * @param hours how long the access lasts (1 to 72)
 */
public record GrantHistoryAccessCommand(Long petId, Long clinicId, Integer hours) {

    public static final int MIN_HOURS = 1;
    public static final int MAX_HOURS = 72;

    public GrantHistoryAccessCommand {
        if (petId == null || petId <= 0) {
            throw new IllegalArgumentException("Pet ID must be a positive number");
        }
        if (clinicId == null || clinicId <= 0) {
            throw new IllegalArgumentException("Clinic ID must be a positive number");
        }
        if (hours == null || hours < MIN_HOURS || hours > MAX_HOURS) {
            throw new IllegalArgumentException(
                    "Hours must be between " + MIN_HOURS + " and " + MAX_HOURS);
        }
    }
}

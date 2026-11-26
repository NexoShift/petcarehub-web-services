package com.nexoshift.petcarehub.profiles.domain.model.commands;

/**
 * The owner ends an access before it expires.
 *
 * @param petId the pet identifier
 * @param grantId the access grant identifier
 */
public record RevokeHistoryAccessCommand(Long petId, Long grantId) {

    public RevokeHistoryAccessCommand {
        if (petId == null || petId <= 0) {
            throw new IllegalArgumentException("Pet ID must be a positive number");
        }
        if (grantId == null || grantId <= 0) {
            throw new IllegalArgumentException("Grant ID must be a positive number");
        }
    }
}

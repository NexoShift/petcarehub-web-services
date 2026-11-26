package com.nexoshift.petcarehub.profiles.interfaces.rest.resources;

import com.nexoshift.petcarehub.profiles.domain.model.valueobjects.HistoryAccessStatus;

import java.time.Instant;

/**
 * A history access grant. {@code status} is ACTIVE, EXPIRED or REVOKED.
 */
public record HistoryAccessGrantResource(
        Long id,
        Long petId,
        Long clinicId,
        Instant grantedAt,
        Instant expiresAt,
        Instant revokedAt,
        HistoryAccessStatus status) {
}

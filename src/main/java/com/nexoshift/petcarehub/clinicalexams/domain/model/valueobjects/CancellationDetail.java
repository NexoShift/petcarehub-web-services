package com.nexoshift.petcarehub.clinicalexams.domain.model.valueobjects;

import jakarta.persistence.Embeddable;
import java.time.Instant;

@Embeddable
public record CancellationDetail(String reason, Instant cancelledAt) {

    public CancellationDetail {
        if (reason == null || reason.isBlank()) {
            throw new IllegalArgumentException("Cancellation reason is required");
        }
        if (cancelledAt == null) {
            throw new IllegalArgumentException("Cancellation date is required");
        }
        reason = reason.trim();
    }
}
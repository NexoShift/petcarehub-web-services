package com.nexoshift.petcarehub.clinicalattention.domain.model.valueobjects;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.time.Instant;

/** Why and when a consultation was cancelled. */
@Embeddable
public record CancellationDetail(
    @Column(name = "cancellation_reason", length = ClinicalFieldLimits.CANCELLATION_REASON)
    String reason,
    @Column(name = "cancelled_at") Instant cancelledAt) {

  public CancellationDetail {
    if (reason == null || reason.isBlank()) {
      throw new IllegalArgumentException("Cancellation reason is required");
    }
    if (reason.length() > ClinicalFieldLimits.CANCELLATION_REASON) {
      throw new IllegalArgumentException("Cancellation reason must not exceed "
          + ClinicalFieldLimits.CANCELLATION_REASON + " characters");
    }
    if (cancelledAt == null) {
      throw new IllegalArgumentException("Cancellation date is required");
    }
  }
}

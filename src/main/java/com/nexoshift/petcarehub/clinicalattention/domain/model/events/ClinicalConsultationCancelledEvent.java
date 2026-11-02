package com.nexoshift.petcarehub.clinicalattention.domain.model.events;

import java.time.Instant;

public record ClinicalConsultationCancelledEvent(
    Long consultationId, String reason, Instant cancelledAt) {
}

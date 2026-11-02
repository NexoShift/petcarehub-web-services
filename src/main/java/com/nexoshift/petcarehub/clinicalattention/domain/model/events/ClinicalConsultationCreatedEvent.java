package com.nexoshift.petcarehub.clinicalattention.domain.model.events;

import java.time.Instant;

public record ClinicalConsultationCreatedEvent(
    Long consultationId, Long petId, Long veterinarianId, Instant occurredAt) {
}

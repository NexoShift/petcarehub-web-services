package com.nexoshift.petcarehub.clinicalattention.domain.model.events;

import java.time.Instant;

/** A consultation was completed. Medical History uses it to build the longitudinal record. */
public record ClinicalConsultationCompletedEvent(
    Long consultationId, Long petId, Long veterinarianId, String primaryDiagnosis,
    Instant completedAt) {
}

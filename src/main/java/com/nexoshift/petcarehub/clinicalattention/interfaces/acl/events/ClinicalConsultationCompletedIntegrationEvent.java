package com.nexoshift.petcarehub.clinicalattention.interfaces.acl.events;

import java.time.Instant;

/**
 * Published contract: other contexts (for example Medical History) listen to this event. It only
 * carries simple types.
 */
public record ClinicalConsultationCompletedIntegrationEvent(
    Long consultationId, Long petId, Long veterinarianId, String primaryDiagnosis,
    Instant completedAt) {
}

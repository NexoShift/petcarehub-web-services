package com.nexoshift.petcarehub.clinicalattention.interfaces.acl.events;

import java.time.Instant;

/**
 * Published contract: the result of an exam was validated by a veterinarian. Other contexts
 * (for example Medical History) listen to it. It only carries simple types.
 */
public record ExamResultValidatedIntegrationEvent(
    Long examId, Long petId, Long clinicId, Long consultationId, String examType,
    String resultSummary, Instant validatedAt) {
}

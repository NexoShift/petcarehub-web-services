package com.nexoshift.petcarehub.clinicalexams.domain.model.events;

import java.time.Instant;

public record ExamResultValidatedEvent(
        Long examId, Long resultId, Long petId, Instant validatedAt) {
}
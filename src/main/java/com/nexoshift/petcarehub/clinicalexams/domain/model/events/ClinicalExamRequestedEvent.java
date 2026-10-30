package com.nexoshift.petcarehub.clinicalexams.domain.model.events;

import java.time.Instant;

public record ClinicalExamRequestedEvent(
        Long examId, Long petId, Long consultationId, Instant requestedAt) {
}
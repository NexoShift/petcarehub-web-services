package com.nexoshift.petcarehub.clinicalexams.interfaces.rest.resources;

import java.time.Instant;
import java.util.List;

public record ClinicalExamResource(
        Long id, Long consultationId, Long clinicId, Long petId, Long requestedBy, String type, String indication,
        String status, Instant requestedAt, Instant startedAt, Instant completedAt,
        String cancellationReason, Instant cancelledAt, ExamResultResource result,
        List<ClinicalFileResource> files) {
}

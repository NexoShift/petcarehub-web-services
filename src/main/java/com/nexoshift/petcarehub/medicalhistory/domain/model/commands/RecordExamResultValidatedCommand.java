package com.nexoshift.petcarehub.medicalhistory.domain.model.commands;

import java.time.Instant;

/** Comes from the integration event of Clinical Exams. */
public record RecordExamResultValidatedCommand(Long examId, Long petId, Long clinicId,
                                               Long consultationId, String examType,
                                               String resultSummary, Instant validatedAt) {
}

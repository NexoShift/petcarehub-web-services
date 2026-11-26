package com.nexoshift.petcarehub.medicalhistory.domain.model.commands;

import java.time.Instant;

/** Comes from the integration event of Clinical Attention. */
public record RecordConsultationCompletedCommand(Long consultationId, Long petId, Long clinicId,
                                                 Long veterinarianId, String primaryDiagnosis,
                                                 Instant completedAt) {
}

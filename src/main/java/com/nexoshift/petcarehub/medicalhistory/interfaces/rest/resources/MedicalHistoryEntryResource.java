package com.nexoshift.petcarehub.medicalhistory.interfaces.rest.resources;

import com.nexoshift.petcarehub.medicalhistory.domain.model.valueobjects.HistoryEntryType;

import java.time.Instant;

/**
 * {@code referenceId} is the consultation id (CONSULTATION_COMPLETED) or the exam id
 * (EXAM_RESULT_VALIDATED): use it to open the full record in its own endpoint.
 */
public record MedicalHistoryEntryResource(Long id, HistoryEntryType type, Long referenceId,
                                          Long consultationId, Long clinicId,
                                          Long veterinarianId, String examType, String summary,
                                          Instant occurredAt) {
}

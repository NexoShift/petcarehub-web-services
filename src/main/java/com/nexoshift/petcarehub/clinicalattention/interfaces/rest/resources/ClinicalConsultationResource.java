package com.nexoshift.petcarehub.clinicalattention.interfaces.rest.resources;

import com.nexoshift.petcarehub.clinicalattention.domain.model.valueobjects.ConsultationStatus;
import java.time.Instant;
import java.util.List;

public record ClinicalConsultationResource(
    Long id, Long petId, Long veterinarianId, Long clinicId, Long consultingRoomId,
    String reason, ConsultationStatus status, Instant startedAt, Instant completedAt,
    String cancellationReason, Instant cancelledAt, AnamnesisResource anamnesis,
    List<PhysiologicalAssessmentResource> physiologicalAssessments,
    List<ClinicalFindingResource> findings, List<DiagnosisResource> diagnoses,
    Instant createdAt, Instant updatedAt) {
}

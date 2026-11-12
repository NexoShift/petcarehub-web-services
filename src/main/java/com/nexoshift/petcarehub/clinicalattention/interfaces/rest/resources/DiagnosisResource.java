package com.nexoshift.petcarehub.clinicalattention.interfaces.rest.resources;

import com.nexoshift.petcarehub.clinicalattention.domain.model.valueobjects.DiagnosisStatus;
import com.nexoshift.petcarehub.clinicalattention.domain.model.valueobjects.DiagnosisType;
import java.time.Instant;
import java.util.List;

/** {@code previousDiagnosisId} is set when the diagnosis is a reevaluation of another one. */
public record DiagnosisResource(
    Long id, String description, DiagnosisType type, boolean primary,
    List<String> differentialDiagnoses, String rationale, DiagnosisStatus status,
    Long previousDiagnosisId, Long diagnosedByVeterinarianId, Instant diagnosedAt) {
}

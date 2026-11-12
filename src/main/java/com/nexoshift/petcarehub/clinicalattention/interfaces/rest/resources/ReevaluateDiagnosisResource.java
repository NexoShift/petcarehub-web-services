package com.nexoshift.petcarehub.clinicalattention.interfaces.rest.resources;

import com.nexoshift.petcarehub.clinicalattention.domain.model.valueobjects.ClinicalFieldLimits;
import com.nexoshift.petcarehub.clinicalattention.domain.model.valueobjects.DiagnosisType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

/** The previous diagnosis is kept as SUPERSEDED; this creates a new one (CON-01). */
public record ReevaluateDiagnosisResource(
    @NotBlank @Size(max = ClinicalFieldLimits.DIAGNOSIS_DESCRIPTION) String description,
    @NotNull DiagnosisType type,
    @Size(max = ClinicalFieldLimits.MAX_DIFFERENTIAL_DIAGNOSES)
    List<@NotBlank @Size(max = ClinicalFieldLimits.DIFFERENTIAL_DIAGNOSIS) String>
        differentialDiagnoses,
    @Size(max = ClinicalFieldLimits.RATIONALE) String rationale) {
}

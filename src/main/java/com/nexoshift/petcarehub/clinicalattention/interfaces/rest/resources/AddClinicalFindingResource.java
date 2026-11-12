package com.nexoshift.petcarehub.clinicalattention.interfaces.rest.resources;

import com.nexoshift.petcarehub.clinicalattention.domain.model.valueobjects.BodySystem;
import com.nexoshift.petcarehub.clinicalattention.domain.model.valueobjects.ClinicalFieldLimits;
import com.nexoshift.petcarehub.clinicalattention.domain.model.valueobjects.FindingSeverity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record AddClinicalFindingResource(
    @NotNull BodySystem bodySystem,
    @NotBlank @Size(max = ClinicalFieldLimits.FINDING_DESCRIPTION) String description,
    FindingSeverity severity) {
}

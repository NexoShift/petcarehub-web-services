package com.nexoshift.petcarehub.clinicalattention.interfaces.rest.resources;

import com.nexoshift.petcarehub.clinicalattention.domain.model.valueobjects.ClinicalFieldLimits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CancelClinicalConsultationResource(
    @NotBlank @Size(max = ClinicalFieldLimits.CANCELLATION_REASON) String reason) {
}

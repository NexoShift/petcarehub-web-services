package com.nexoshift.petcarehub.clinicalattention.interfaces.rest.resources;

import com.nexoshift.petcarehub.clinicalattention.domain.model.valueobjects.ClinicalFieldLimits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/** Dates as yyyy-MM-dd. {@code endDate} is optional (treatment without a known end). */
public record RegisterTreatmentResource(
    @NotBlank @Size(max = ClinicalFieldLimits.TREATMENT_DESCRIPTION) String description,
    @NotBlank @Size(max = ClinicalFieldLimits.TREATMENT_INSTRUCTION) String instruction,
    @NotNull LocalDate startDate,
    LocalDate endDate) {
}

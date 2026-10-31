package com.nexoshift.petcarehub.clinicalattention.interfaces.rest.resources;

import com.nexoshift.petcarehub.clinicalattention.domain.model.valueobjects.ClinicalFieldLimits;
import com.nexoshift.petcarehub.clinicalattention.domain.model.valueobjects.HydrationStatus;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

/** One measurement of the pet. Each request adds a new assessment to the consultation. */
public record RegisterPhysiologicalAssessmentResource(
    @NotNull @Positive BigDecimal weightKg,
    @NotNull BigDecimal temperatureCelsius,
    @NotNull @Positive Integer heartRateBpm,
    @NotNull @Positive Integer respiratoryRateBpm,
    @Min(1) @Max(9) Integer bodyConditionScore,
    HydrationStatus hydrationStatus,
    @Size(max = ClinicalFieldLimits.MUCOUS_MEMBRANES) String mucousMembranes,
    @Positive BigDecimal capillaryRefillTimeSeconds,
    @Size(max = ClinicalFieldLimits.ASSESSMENT_OBSERVATIONS) String observations) {
}

package com.nexoshift.petcarehub.clinicalattention.interfaces.rest.resources;

import com.nexoshift.petcarehub.clinicalattention.domain.model.valueobjects.HydrationStatus;
import java.math.BigDecimal;
import java.time.Instant;

public record PhysiologicalAssessmentResource(
    Long id, BigDecimal weightKg, BigDecimal temperatureCelsius, Integer heartRateBpm,
    Integer respiratoryRateBpm, Integer bodyConditionScore, HydrationStatus hydrationStatus,
    String mucousMembranes, BigDecimal capillaryRefillTimeSeconds, String observations,
    Instant recordedAt) {
}

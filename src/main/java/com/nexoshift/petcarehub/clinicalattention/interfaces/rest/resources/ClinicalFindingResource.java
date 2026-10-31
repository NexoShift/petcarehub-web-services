package com.nexoshift.petcarehub.clinicalattention.interfaces.rest.resources;

import com.nexoshift.petcarehub.clinicalattention.domain.model.valueobjects.BodySystem;
import com.nexoshift.petcarehub.clinicalattention.domain.model.valueobjects.FindingSeverity;
import java.time.Instant;

public record ClinicalFindingResource(
    Long id, BodySystem bodySystem, String description, FindingSeverity severity,
    Instant recordedAt) {
}

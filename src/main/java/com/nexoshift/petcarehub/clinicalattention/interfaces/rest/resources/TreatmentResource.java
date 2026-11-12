package com.nexoshift.petcarehub.clinicalattention.interfaces.rest.resources;

import com.nexoshift.petcarehub.clinicalattention.domain.model.valueobjects.TreatmentStatus;

import java.time.LocalDateTime;

public record TreatmentResource(
        Long id,
        String description,
        String instruction,
        LocalDateTime startDateTime,
        LocalDateTime endDateTime,
        TreatmentStatus treatmentStatus) {
}

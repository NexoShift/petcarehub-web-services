package com.nexoshift.petcarehub.clinicalattention.interfaces.rest.resources;

import com.nexoshift.petcarehub.clinicalattention.domain.model.valueobjects.TreatmentStatus;

import java.time.Instant;
import java.time.LocalDate;

public record TreatmentResource(
    Long id, String description, String instruction, LocalDate startDate, LocalDate endDate,
    TreatmentStatus status, Long prescribedByVeterinarianId, Instant createdAt) {
}

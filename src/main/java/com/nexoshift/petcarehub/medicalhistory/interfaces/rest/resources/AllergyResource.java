package com.nexoshift.petcarehub.medicalhistory.interfaces.rest.resources;

import com.nexoshift.petcarehub.medicalhistory.domain.model.valueobjects.AllergySeverity;
import com.nexoshift.petcarehub.medicalhistory.domain.model.valueobjects.ReporterType;

import java.time.Instant;

public record AllergyResource(Long id, String allergen, String reaction, AllergySeverity severity,
                              String notes, ReporterType reportedBy, Instant recordedAt) {
}

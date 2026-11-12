package com.nexoshift.petcarehub.clinicalexams.interfaces.rest.resources;

import java.time.Instant;
import java.time.LocalDate;

public record ExamResultResource(
        Long id, String summary, String values, String interpretation, LocalDate resultDate,
        boolean validated, Long validatedBy, Instant validatedAt) {
}

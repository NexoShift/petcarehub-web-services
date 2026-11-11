package com.nexoshift.petcarehub.clinicalexams.interfaces.rest.resources;

import com.nexoshift.petcarehub.clinicalexams.domain.model.valueobjects.ExamType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record RequestClinicalExamResource(
        @NotNull @Positive Long consultationId,
        @NotNull ExamType type,
        @Size(max = 500) String indication) {
}

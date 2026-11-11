package com.nexoshift.petcarehub.clinicalexams.interfaces.rest.resources;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CancelClinicalExamResource(@NotBlank @Size(max = 500) String reason) {
}

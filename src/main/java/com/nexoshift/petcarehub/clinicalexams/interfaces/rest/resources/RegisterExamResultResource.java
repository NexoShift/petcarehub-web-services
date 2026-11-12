package com.nexoshift.petcarehub.clinicalexams.interfaces.rest.resources;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record RegisterExamResultResource(
        @NotBlank @Size(max = 2000) String summary,
        @Size(max = 4000) String values,
        @Size(max = 2000) String interpretation,
        @NotNull @PastOrPresent LocalDate resultDate) {
}

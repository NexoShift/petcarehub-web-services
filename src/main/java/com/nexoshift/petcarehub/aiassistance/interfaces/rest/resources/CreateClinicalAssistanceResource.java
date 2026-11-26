package com.nexoshift.petcarehub.aiassistance.interfaces.rest.resources;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.util.List;

/** The veterinarian id is not here: it comes from the token. */
public record CreateClinicalAssistanceResource(
    @NotNull @Positive Long consultationId,
    @NotNull @Positive Long petId,
    @NotBlank @Size(max = 1000) String currentReason,
    @NotNull @Size(max = 10) List<@Valid SelectedBackgroundResource> backgrounds) {
}

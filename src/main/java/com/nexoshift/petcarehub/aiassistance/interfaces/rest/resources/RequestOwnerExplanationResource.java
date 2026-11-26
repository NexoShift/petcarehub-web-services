package com.nexoshift.petcarehub.aiassistance.interfaces.rest.resources;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/** The owner id is not here: it comes from the token. consultationId is optional. */
public record RequestOwnerExplanationResource(
    @NotNull @Positive Long petId,
    @Positive Long consultationId,
    @NotBlank @Size(max = 1000) String question) {
}

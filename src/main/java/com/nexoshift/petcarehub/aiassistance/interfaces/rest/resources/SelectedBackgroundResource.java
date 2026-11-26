package com.nexoshift.petcarehub.aiassistance.interfaces.rest.resources;

import com.nexoshift.petcarehub.aiassistance.domain.model.valueobjects.BackgroundType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record SelectedBackgroundResource(
    @NotNull BackgroundType type,
    @NotNull @Positive Long referenceId) {
}

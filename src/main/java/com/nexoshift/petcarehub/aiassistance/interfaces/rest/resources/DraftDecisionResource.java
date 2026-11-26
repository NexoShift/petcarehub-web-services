package com.nexoshift.petcarehub.aiassistance.interfaces.rest.resources;

import com.nexoshift.petcarehub.aiassistance.domain.model.valueobjects.DraftDecision;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record DraftDecisionResource(
    @NotNull DraftDecision decision,
    @Size(max = 4000) String editedText) {
}

package com.nexoshift.petcarehub.aiassistance.interfaces.rest.resources;

import java.time.Instant;

public record OwnerExplanationResource(
    Long id,
    Long petId,
    Long consultationId,
    String question,
    String answer,
    String model,
    Instant createdAt) {
}

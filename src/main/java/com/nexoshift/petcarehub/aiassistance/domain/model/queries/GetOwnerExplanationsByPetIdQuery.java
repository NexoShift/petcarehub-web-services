package com.nexoshift.petcarehub.aiassistance.domain.model.queries;

/** The conversation of one owner about one pet. */
public record GetOwnerExplanationsByPetIdQuery(Long petId, Long ownerUserId) {
}

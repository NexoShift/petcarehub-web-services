package com.nexoshift.petcarehub.profiles.domain.model.queries;

/**
 * Access grants of a pet. The owner sees all of them; a veterinarian only those of
 * their clinic.
 *
 * @param petId the pet identifier
 */
public record GetHistoryAccessGrantsByPetIdQuery(Long petId) {
}

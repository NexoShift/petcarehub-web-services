package com.nexoshift.petcarehub.profiles.interfaces.rest.resources;

import com.nexoshift.petcarehub.profiles.domain.model.valueobjects.Species;

/**
 * Resource containing basic pet information for patient registration.
 *
 * @param id the pet identifier
 * @param petCode the unique pet identification code
 * @param name the pet name
 * @param species the pet species
 * @param breed the pet breed
 */
public record PetSearchResource(
        Long id,
        String petCode,
        String name,
        Species species,
        String breed
) {
}

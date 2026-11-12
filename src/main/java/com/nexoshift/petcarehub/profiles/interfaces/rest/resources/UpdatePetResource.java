package com.nexoshift.petcarehub.profiles.interfaces.rest.resources;

import com.nexoshift.petcarehub.profiles.domain.model.valueobjects.PetSex;
import com.nexoshift.petcarehub.profiles.domain.model.valueobjects.Species;

import java.time.LocalDate;

/**
 * Resource containing the information required to update a pet.
 *
 * @param name the pet's name
 * @param species the pet's species
 * @param breed the pet's breed
 * @param birthDate the pet's birth date
 * @param sex the pet's sex
 */
public record UpdatePetResource(
        String name,
        Species species,
        String breed,
        LocalDate birthDate,
        PetSex sex
) {
}

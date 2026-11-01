package com.nexoshift.petcarehub.profiles.interfaces.rest.resources;

import com.nexoshift.petcarehub.profiles.domain.model.valueobjects.PetSex;
import com.nexoshift.petcarehub.profiles.domain.model.valueobjects.Species;

import java.time.LocalDate;

/**
 * Resource representing a pet profile.
 *
 * @param id the pet identifier
 * @param ownerId the pet owner's profile identifier
 * @param name the pet's name
 * @param species the pet's species
 * @param breed the pet's breed
 * @param birthDate the pet's birth date
 * @param sex the pet's sex
 * @param profileImageUrl the temporary URL of the profile image
 */
public record PetResource(
        Long id,
        Long ownerId,
        String name,
        Species species,
        String breed,
        LocalDate birthDate,
        PetSex sex,
        String profileImageUrl
) {
}

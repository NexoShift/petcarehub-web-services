package com.nexoshift.petcarehub.profiles.interfaces.rest.resources;


import com.nexoshift.petcarehub.profiles.domain.model.valueobjects.PetSex;
import com.nexoshift.petcarehub.profiles.domain.model.valueobjects.Species;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;

/**
 * Resource containing the information required to create a pet.
 *
 * @param ownerId the pet owner's profile identifier
 * @param name the pet's name
 * @param species the pet's species
 * @param breed the pet's breed
 * @param birthDate the pet's birth date
 * @param sex the pet's sex
 * @param image the optional profile image
 */
public record CreatePetResource(
        Long ownerId,
        String name,
        Species species,
        String breed,
        LocalDate birthDate,
        PetSex sex,
        MultipartFile image
) {
}

package com.nexoshift.petcarehub.profiles.domain.model.commands;

import com.nexoshift.petcarehub.profiles.domain.model.valueobjects.PetSex;
import com.nexoshift.petcarehub.profiles.domain.model.valueobjects.Species;

import java.time.LocalDate;

/**
 * Command to create a pet profile.
 *
 * @param ownerId the pet owner's profile identifier
 * @param name the pet's name
 * @param species the pet's species
 * @param breed the pet's breed
 * @param birthDate the pet's birth date
 * @param sex the pet's sex
 * @param profileImageKey the S3 object key of the profile image
 */
public record CreatePetCommand(Long ownerId, String name, Species species, String breed,
        LocalDate birthDate, PetSex sex, String profileImageKey) {
    public CreatePetCommand {
        if (ownerId == null || ownerId <= 0) {
            throw new IllegalArgumentException("Owner ID must be a positive number");
        }

        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Pet name is required");
        }

        if (species == null) {
            throw new IllegalArgumentException("Pet species is required");
        }

        if (birthDate != null && birthDate.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Birth date cannot be in the future");
        }

        name = name.trim();
    }
}

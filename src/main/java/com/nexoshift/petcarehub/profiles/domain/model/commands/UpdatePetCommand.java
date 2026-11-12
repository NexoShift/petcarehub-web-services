package com.nexoshift.petcarehub.profiles.domain.model.commands;

import com.nexoshift.petcarehub.profiles.domain.model.valueobjects.PetSex;
import com.nexoshift.petcarehub.profiles.domain.model.valueobjects.Species;

import java.time.LocalDate;

/**
 * Command to update a pet's profile information.
 *
 * @param petId the pet profile identifier
 * @param name the pet's name
 * @param species the pet's species
 * @param breed the pet's breed
 * @param birthDate the pet's birth date
 * @param sex the pet's sex
 */
public record UpdatePetCommand(Long petId, String name, Species species,
        String breed, LocalDate birthDate, PetSex sex) {
    public UpdatePetCommand {
        if (petId == null || petId <= 0) {
            throw new IllegalArgumentException("Pet ID must be a positive number");
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

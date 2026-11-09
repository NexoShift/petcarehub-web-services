package com.nexoshift.petcarehub.profiles.interfaces.acl;

import java.time.LocalDate;

/**
 * Read-only view of a pet for other bounded contexts.
 * It carries no owner contact data.
 *
 * @param petId the pet identifier
 * @param name the pet's name
 * @param species the species (DOG, CAT, OTHER)
 * @param breed the pet's breed
 * @param sex the pet's sex (MALE, FEMALE)
 * @param birthDate the pet's birth date, or null if unknown
 */
public record PetProfileSnapshot(
        Long petId,
        String name,
        String species,
        String breed,
        String sex,
        LocalDate birthDate
) {
}

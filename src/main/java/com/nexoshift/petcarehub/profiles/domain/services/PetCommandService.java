package com.nexoshift.petcarehub.profiles.domain.services;

import com.nexoshift.petcarehub.profiles.domain.model.aggregates.Pet;
import com.nexoshift.petcarehub.profiles.domain.model.commands.CreatePetCommand;
import com.nexoshift.petcarehub.profiles.domain.model.commands.UpdatePetCommand;
import com.nexoshift.petcarehub.profiles.domain.model.commands.UpdatePetImageCommand;

import java.util.Optional;

/**
 * Service interface for handling pet commands.
 */
public interface PetCommandService {

    /**
     * Creates a pet profile.
     *
     * @param command the create pet command
     * @return the created pet identifier
     */
    Long handle(CreatePetCommand command);

    /**
     * Updates a pet's profile information.
     *
     * @param command the update pet command
     * @return the updated pet, if found
     */
    Optional<Pet> handle(UpdatePetCommand command);

    /**
     * Updates a pet's profile image.
     *
     * @param command the update pet image command
     * @return the updated pet, if found
     */
    Optional<Pet> handle(UpdatePetImageCommand command);
}

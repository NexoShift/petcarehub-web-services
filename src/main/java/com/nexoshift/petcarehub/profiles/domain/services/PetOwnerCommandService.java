package com.nexoshift.petcarehub.profiles.domain.services;

import com.nexoshift.petcarehub.profiles.domain.model.aggregates.PetOwner;
import com.nexoshift.petcarehub.profiles.domain.model.commands.CreatePetOwnerCommand;
import com.nexoshift.petcarehub.profiles.domain.model.commands.UpdatePetOwnerCommand;
import com.nexoshift.petcarehub.profiles.domain.model.commands.UpdatePetOwnerImageCommand;

import java.util.Optional;

/**
 * Service interface for handling pet owner commands.
 */
public interface PetOwnerCommandService {

    /**
     * Creates a pet owner profile.
     *
     * @param command the create pet owner command
     * @return the created pet owner identifier
     */
    Long handle(CreatePetOwnerCommand command);

    /**
     * Updates a pet owner's personal information.
     *
     * @param command the update pet owner command
     * @return the updated pet owner, if found
     */
    Optional<PetOwner> handle(UpdatePetOwnerCommand command);

    /**
     * Updates a pet owner's profile image.
     *
     * @param command the update pet owner image command
     * @return the updated pet owner, if found
     */
    Optional<PetOwner> handle(UpdatePetOwnerImageCommand command);
}

package com.nexoshift.petcarehub.profiles.domain.services;

import com.nexoshift.petcarehub.profiles.domain.model.aggregates.Veterinarian;
import com.nexoshift.petcarehub.profiles.domain.model.commands.CreateVeterinarianCommand;
import com.nexoshift.petcarehub.profiles.domain.model.commands.UpdateVeterinarianCommand;
import com.nexoshift.petcarehub.profiles.domain.model.commands.UpdateVeterinarianImageCommand;

import java.util.Optional;

/**
 * Service interface for handling veterinarian commands.
 */
public interface VeterinarianCommandService {

    /**
     * Creates a veterinarian profile.
     *
     * @param command the create veterinarian command
     * @return the created veterinarian identifier
     */
    Long handle(CreateVeterinarianCommand command);

    /**
     * Updates a veterinarian's professional information.
     *
     * @param command the update veterinarian command
     * @return the updated veterinarian, if found
     */
    Optional<Veterinarian> handle(UpdateVeterinarianCommand command);

    /**
     * Updates a veterinarian's profile image.
     *
     * @param command the update veterinarian image command
     * @return the updated veterinarian, if found
     */
    Optional<Veterinarian> handle(UpdateVeterinarianImageCommand command);
}

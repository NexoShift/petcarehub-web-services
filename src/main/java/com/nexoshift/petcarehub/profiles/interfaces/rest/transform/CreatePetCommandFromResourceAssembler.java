package com.nexoshift.petcarehub.profiles.interfaces.rest.transform;

import com.nexoshift.petcarehub.profiles.domain.model.commands.CreatePetCommand;
import com.nexoshift.petcarehub.profiles.interfaces.rest.resources.CreatePetResource;

/**
 * Assembler for creating a pet command from a resource.
 */
public class CreatePetCommandFromResourceAssembler {

    /**
     * Converts a resource into a create pet command.
     *
     * @param resource the create pet resource
     * @param profileImageKey the generated S3 object key
     * @return the create pet command
     */
    public static CreatePetCommand toCommandFromResource(
            CreatePetResource resource,
            String profileImageKey) {

        return new CreatePetCommand(
                resource.ownerId(),
                resource.name(),
                resource.species(),
                resource.breed(),
                resource.birthDate(),
                resource.sex(),
                profileImageKey
        );
    }
}

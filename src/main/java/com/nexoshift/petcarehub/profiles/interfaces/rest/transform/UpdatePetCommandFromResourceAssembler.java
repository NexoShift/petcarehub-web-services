package com.nexoshift.petcarehub.profiles.interfaces.rest.transform;

import com.nexoshift.petcarehub.profiles.domain.model.commands.UpdatePetCommand;
import com.nexoshift.petcarehub.profiles.interfaces.rest.resources.UpdatePetResource;

/**
 * Assembler for creating an update pet command from a resource.
 */
public class UpdatePetCommandFromResourceAssembler {

    /**
     * Converts a resource into an update pet command.
     *
     * @param petId the pet identifier
     * @param resource the update pet resource
     * @return the update pet command
     */
    public static UpdatePetCommand toCommandFromResource(
            Long petId,
            UpdatePetResource resource) {

        return new UpdatePetCommand(
                petId,
                resource.name(),
                resource.species(),
                resource.breed(),
                resource.birthDate(),
                resource.sex()
        );
    }
}

package com.nexoshift.petcarehub.profiles.interfaces.rest.transform;

import com.nexoshift.petcarehub.profiles.domain.model.commands.UpdatePetOwnerCommand;
import com.nexoshift.petcarehub.profiles.interfaces.rest.resources.UpdatePetOwnerResource;

/**
 * Assembler for creating an update pet owner command from a resource.
 */
public class UpdatePetOwnerCommandFromResourceAssembler {

    /**
     * Converts a resource and profile identifier into an update command.
     *
     * @param petOwnerId the pet owner profile identifier
     * @param resource the update pet owner resource
     * @return the update pet owner command
     */
    public static UpdatePetOwnerCommand toCommandFromResource(
            Long petOwnerId,
            UpdatePetOwnerResource resource) {

        return new UpdatePetOwnerCommand(
                petOwnerId,
                resource.firstName(),
                resource.lastName(),
                resource.phoneNumber(),
                resource.sex()
        );
    }
}

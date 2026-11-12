package com.nexoshift.petcarehub.profiles.interfaces.rest.transform;

import com.nexoshift.petcarehub.profiles.domain.model.commands.CreatePetOwnerCommand;
import com.nexoshift.petcarehub.profiles.interfaces.rest.resources.CreatePetOwnerResource;

/**
 * Assembler for creating a pet owner command from a resource.
 */
public class CreatePetOwnerCommandFromResourceAssembler {

    /**
     * Converts a resource into a create pet owner command.
     *
     * @param resource the created pet owner resource
     * @param profileImageKey the uploaded image's S3 object key
     * @return the create pet owner command
     */
    public static CreatePetOwnerCommand toCommandFromResource(
            CreatePetOwnerResource resource, String profileImageKey) {

        return new CreatePetOwnerCommand(
                resource.userId(),
                resource.firstName(),
                resource.lastName(),
                resource.email(),
                resource.phoneNumber(),
                resource.sex(),
                profileImageKey
        );
    }
}

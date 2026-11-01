package com.nexoshift.petcarehub.profiles.interfaces.rest.transform;

import com.nexoshift.petcarehub.profiles.domain.model.commands.CreateVeterinarianCommand;
import com.nexoshift.petcarehub.profiles.interfaces.rest.resources.CreateVeterinarianResource;

/**
 * Assembler for creating a veterinarian command from a resource.
 */
public class CreateVeterinarianCommandFromResourceAssembler {

    public static CreateVeterinarianCommand toCommandFromResource(
            CreateVeterinarianResource resource,
            String profileImageKey) {

        return new CreateVeterinarianCommand(
                resource.userId(),
                resource.clinicId(),
                resource.firstName(),
                resource.lastName(),
                resource.email(),
                resource.phoneNumber(),
                resource.sex(),
                resource.licenseNumber(),
                resource.specialty(),
                profileImageKey
        );
    }
}

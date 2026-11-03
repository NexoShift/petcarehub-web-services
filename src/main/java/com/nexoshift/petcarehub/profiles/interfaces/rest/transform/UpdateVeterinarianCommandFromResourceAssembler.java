package com.nexoshift.petcarehub.profiles.interfaces.rest.transform;

import com.nexoshift.petcarehub.profiles.domain.model.commands.UpdateVeterinarianCommand;
import com.nexoshift.petcarehub.profiles.interfaces.rest.resources.UpdateVeterinarianResource;

/**
 * Assembler for creating an update veterinarian command from a resource.
 */
public class UpdateVeterinarianCommandFromResourceAssembler {

    public static UpdateVeterinarianCommand toCommandFromResource(
            Long veterinarianId,
            UpdateVeterinarianResource resource) {

        return new UpdateVeterinarianCommand(
                veterinarianId,
                resource.firstName(),
                resource.lastName(),
                resource.phoneNumber(),
                resource.sex(),
                resource.licenseNumber(),
                resource.specialty()
        );
    }
}
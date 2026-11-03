package com.nexoshift.petcarehub.profiles.interfaces.rest.transform;

import com.nexoshift.petcarehub.profiles.domain.model.commands.CreateClinicCommand;
import com.nexoshift.petcarehub.profiles.interfaces.rest.resources.CreateClinicResource;

/**
 * Assembler for creating a clinic command from a resource.
 */
public class CreateClinicCommandFromResourceAssembler {

    public static CreateClinicCommand toCommandFromResource(
            CreateClinicResource resource,
            String profileImageKey) {

        return new CreateClinicCommand(
                resource.accountId(),
                resource.name(),
                resource.ruc(),
                resource.address(),
                resource.phoneNumber(),
                resource.description(),
                profileImageKey
        );
    }
}

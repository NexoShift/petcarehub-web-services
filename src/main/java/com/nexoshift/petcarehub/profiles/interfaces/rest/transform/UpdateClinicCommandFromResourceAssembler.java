package com.nexoshift.petcarehub.profiles.interfaces.rest.transform;

import com.nexoshift.petcarehub.profiles.domain.model.commands.UpdateClinicCommand;
import com.nexoshift.petcarehub.profiles.interfaces.rest.resources.UpdateClinicResource;

/**
 * Assembler for creating an update clinic command from a resource.
 */
public class UpdateClinicCommandFromResourceAssembler {

    public static UpdateClinicCommand toCommandFromResource(
            Long clinicId,
            UpdateClinicResource resource) {

        return new UpdateClinicCommand(
                clinicId,
                resource.name(),
                resource.ruc(),
                resource.address(),
                resource.phoneNumber(),
                resource.description()
        );
    }
}

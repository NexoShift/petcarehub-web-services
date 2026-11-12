package com.nexoshift.petcarehub.profiles.interfaces.rest.transform;

import com.nexoshift.petcarehub.profiles.domain.model.commands.CreateClinicPatientCommand;
import com.nexoshift.petcarehub.profiles.interfaces.rest.resources.CreateClinicPatientResource;

/**
 * Assembler for converting clinic patient resources into commands.
 */
public class CreateClinicPatientCommandFromResourceAssembler {

    /**
     * Converts a resource into a create clinic patient command.
     *
     * @param clinicId the clinic identifier
     * @param resource the create clinic patient resource
     * @return the create clinic patient command
     */
    public static CreateClinicPatientCommand toCommandFromResource(
            Long clinicId,
            CreateClinicPatientResource resource) {

        return new CreateClinicPatientCommand(
                clinicId,
                resource.petId()
        );
    }
}

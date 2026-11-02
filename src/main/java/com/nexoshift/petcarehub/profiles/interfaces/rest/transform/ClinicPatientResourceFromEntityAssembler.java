package com.nexoshift.petcarehub.profiles.interfaces.rest.transform;

import com.nexoshift.petcarehub.profiles.domain.model.aggregates.ClinicPatient;
import com.nexoshift.petcarehub.profiles.interfaces.rest.resources.ClinicPatientResource;

/**
 * Assembler for converting clinic patient entities into resources.
 */
public class ClinicPatientResourceFromEntityAssembler {

    /**
     * Converts a clinic patient entity into a resource.
     *
     * @param entity the clinic patient entity
     * @return the clinic patient resource
     */
    public static ClinicPatientResource toResourceFromEntity(
            ClinicPatient entity) {

        return new ClinicPatientResource(
                entity.getId(),
                entity.getClinicId().clinicId(),
                entity.getPetId().petId(),
                entity.getRegisteredAt()
        );
    }
}

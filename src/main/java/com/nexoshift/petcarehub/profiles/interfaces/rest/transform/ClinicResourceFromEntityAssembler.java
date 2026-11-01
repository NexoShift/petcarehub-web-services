package com.nexoshift.petcarehub.profiles.interfaces.rest.transform;

import com.nexoshift.petcarehub.profiles.domain.model.aggregates.Clinic;
import com.nexoshift.petcarehub.profiles.interfaces.rest.resources.ClinicResource;

/**
 * Assembler for creating a clinic resource from an aggregate.
 */
public class ClinicResourceFromEntityAssembler {

    public static ClinicResource toResourceFromEntity(
            Clinic entity,
            String profileImageUrl) {

        return new ClinicResource(
                entity.getId(),
                entity.getAccountId().accountId(),
                entity.getName(),
                entity.getRuc(),
                entity.getAddress(),
                entity.getPhoneNumber(),
                entity.getDescription(),
                profileImageUrl
        );
    }
}

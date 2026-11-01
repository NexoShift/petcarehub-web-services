package com.nexoshift.petcarehub.profiles.interfaces.rest.transform;

import com.nexoshift.petcarehub.profiles.domain.model.aggregates.Veterinarian;
import com.nexoshift.petcarehub.profiles.interfaces.rest.resources.VeterinarianResource;

/**
 * Assembler for creating a veterinarian resource from an aggregate.
 */
public class VeterinarianResourceFromEntityAssembler {

    public static VeterinarianResource toResourceFromEntity(
            Veterinarian entity,
            String profileImageUrl) {

        return new VeterinarianResource(
                entity.getId(),
                entity.getUserId().userId(),
                entity.getClinicId().clinicId(),
                entity.getFirstName(),
                entity.getLastName(),
                entity.getEmail(),
                entity.getPhoneNumber(),
                entity.getSex(),
                entity.getLicenseNumber(),
                entity.getSpecialty(),
                profileImageUrl
        );
    }
}

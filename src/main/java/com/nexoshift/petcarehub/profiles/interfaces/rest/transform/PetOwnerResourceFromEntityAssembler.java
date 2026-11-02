package com.nexoshift.petcarehub.profiles.interfaces.rest.transform;

import com.nexoshift.petcarehub.profiles.domain.model.aggregates.PetOwner;
import com.nexoshift.petcarehub.profiles.interfaces.rest.resources.PetOwnerResource;

/**
 * Assembler for creating a pet owner resource from an aggregate.
 */
public class PetOwnerResourceFromEntityAssembler {

    /**
     * Converts a pet owner aggregate into a resource.
     *
     * @param entity the pet owner aggregate
     * @return the pet owner resource
     */
    public static PetOwnerResource toResourceFromEntity(PetOwner entity, String profileImageUrl) {

        return new PetOwnerResource(
                entity.getId(),
                entity.getUserId().userId(),
                entity.getFirstName(),
                entity.getLastName(),
                entity.getEmail(),
                entity.getPhoneNumber(),
                entity.getSex(),
                profileImageUrl
        );
    }
}

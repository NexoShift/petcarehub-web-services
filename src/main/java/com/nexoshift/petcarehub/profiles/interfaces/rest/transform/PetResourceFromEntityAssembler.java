package com.nexoshift.petcarehub.profiles.interfaces.rest.transform;

import com.nexoshift.petcarehub.profiles.domain.model.aggregates.Pet;
import com.nexoshift.petcarehub.profiles.interfaces.rest.resources.PetResource;

/**
 * Assembler for creating a pet resource from an aggregate.
 */
public class PetResourceFromEntityAssembler {

    /**
     * Converts a pet aggregate into a resource.
     *
     * @param entity the pet aggregate
     * @param profileImageUrl the temporary image URL
     * @return the pet resource
     */
    public static PetResource toResourceFromEntity(
            Pet entity,
            String profileImageUrl) {

        return new PetResource(
                entity.getId(),
                entity.getOwnerId().ownerId(),
                entity.getName(),
                entity.getSpecies(),
                entity.getBreed(),
                entity.getBirthDate(),
                entity.getSex(),
                profileImageUrl
        );
    }
}
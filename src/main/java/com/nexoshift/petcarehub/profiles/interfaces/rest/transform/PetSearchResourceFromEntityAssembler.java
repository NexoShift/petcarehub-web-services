package com.nexoshift.petcarehub.profiles.interfaces.rest.transform;

import com.nexoshift.petcarehub.profiles.domain.model.aggregates.Pet;
import com.nexoshift.petcarehub.profiles.interfaces.rest.resources.PetSearchResource;

/**
 * Assembler for converting pet entities into search resources.
 */
public class PetSearchResourceFromEntityAssembler {

    /**
     * Converts a pet entity into a search resource.
     *
     * @param entity the pet entity
     * @return the pet search resource
     */
    public static PetSearchResource toResourceFromEntity(Pet entity) {
        return new PetSearchResource(
                entity.getId(),
                entity.getPetCode().value(),
                entity.getName(),
                entity.getSpecies(),
                entity.getBreed()
        );
    }
}

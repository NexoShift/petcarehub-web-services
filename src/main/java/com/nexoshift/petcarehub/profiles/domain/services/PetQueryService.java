package com.nexoshift.petcarehub.profiles.domain.services;

import com.nexoshift.petcarehub.profiles.domain.model.aggregates.Pet;
import com.nexoshift.petcarehub.profiles.domain.model.queries.GetPetByIdQuery;
import com.nexoshift.petcarehub.profiles.domain.model.queries.GetPetsByOwnerIdQuery;

import java.util.List;
import java.util.Optional;

/**
 * Service interface for handling pet queries.
 */
public interface PetQueryService {

    /**
     * Retrieves a pet by its profile identifier.
     *
     * @param query the query containing the pet identifier
     * @return the pet, if found
     */
    Optional<Pet> handle(GetPetByIdQuery query);

    /**
     * Retrieves all pets belonging to a pet owner.
     *
     * @param query the query containing the owner identifier
     * @return the list of pets belonging to the owner
     */
    List<Pet> handle(GetPetsByOwnerIdQuery query);
}
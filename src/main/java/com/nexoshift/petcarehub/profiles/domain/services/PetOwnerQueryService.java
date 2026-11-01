package com.nexoshift.petcarehub.profiles.domain.services;

import com.nexoshift.petcarehub.profiles.domain.model.aggregates.PetOwner;
import com.nexoshift.petcarehub.profiles.domain.model.queries.GetPetOwnerByIdQuery;
import com.nexoshift.petcarehub.profiles.domain.model.queries.GetPetOwnerByUserIdQuery;

import java.util.Optional;

/**
 * Service interface for handling pet owner queries.
 */
public interface PetOwnerQueryService {

    /**
     * Retrieves a pet owner by its profile identifier.
     *
     * @param query the query containing the pet owner identifier
     * @return the pet owner, if found
     */
    Optional<PetOwner> handle(GetPetOwnerByIdQuery query);

    /**
     * Retrieves a pet owner by its IAM user identifier.
     *
     * @param query the query containing the IAM user identifier
     * @return the pet owner, if found
     */
    Optional<PetOwner> handle(GetPetOwnerByUserIdQuery query);
}

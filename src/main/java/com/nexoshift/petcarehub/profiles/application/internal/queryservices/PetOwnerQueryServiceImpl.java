package com.nexoshift.petcarehub.profiles.application.internal.queryservices;

import com.nexoshift.petcarehub.profiles.domain.model.aggregates.PetOwner;
import com.nexoshift.petcarehub.profiles.domain.model.queries.GetPetOwnerByIdQuery;
import com.nexoshift.petcarehub.profiles.domain.model.queries.GetPetOwnerByUserIdQuery;
import com.nexoshift.petcarehub.profiles.domain.model.valueobjects.UserId;
import com.nexoshift.petcarehub.profiles.domain.services.PetOwnerQueryService;
import com.nexoshift.petcarehub.profiles.infrastructure.persistence.jpa.repositories.PetOwnerRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Implementation of the pet owner query service.
 * Handles queries related to pet owner profiles.
 *
 * @see PetOwnerQueryService
 * @see PetOwnerRepository
 */
@Service
public class PetOwnerQueryServiceImpl implements PetOwnerQueryService {

    private final PetOwnerRepository petOwnerRepository;

    /**
     * Constructs a pet owner query service.
     *
     * @param petOwnerRepository the pet owner repository
     */
    public PetOwnerQueryServiceImpl(PetOwnerRepository petOwnerRepository) {
        this.petOwnerRepository = petOwnerRepository;
    }

    /**
     * Retrieves a pet owner by its profile identifier.
     *
     * @param query the pet owner identifier query
     * @return the pet owner, if found
     */
    @Override
    public Optional<PetOwner> handle(GetPetOwnerByIdQuery query) {
        return petOwnerRepository.findById(query.petOwnerId());
    }

    /**
     * Retrieves a pet owner by its IAM user identifier.
     *
     * @param query the IAM user identifier query
     * @return the pet owner, if found
     */
    @Override
    public Optional<PetOwner> handle(GetPetOwnerByUserIdQuery query) {
        return petOwnerRepository.findByUserId(
                new UserId(query.userId())
        );
    }
}

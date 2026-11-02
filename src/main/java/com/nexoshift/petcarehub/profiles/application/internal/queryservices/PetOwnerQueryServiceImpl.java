package com.nexoshift.petcarehub.profiles.application.internal.queryservices;

import com.nexoshift.petcarehub.profiles.application.internal.outboundservices.acl.IamExternalService;
import com.nexoshift.petcarehub.profiles.domain.exceptions.ProfileOperationNotAllowedException;
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
    private final IamExternalService iamExternalService;

    /**
     * Constructs a pet owner query service.
     *
     * @param petOwnerRepository the pet owner repository
     */
    public PetOwnerQueryServiceImpl(PetOwnerRepository petOwnerRepository, IamExternalService iamExternalService) {
        this.petOwnerRepository = petOwnerRepository;
        this.iamExternalService = iamExternalService;
    }

    /**
     * Retrieves a pet owner by its profile identifier.
     *
     * @param query the pet owner identifier query
     * @return the pet owner, if found
     */
    @Override
    public Optional<PetOwner> handle(GetPetOwnerByIdQuery query) {

        var petOwner = petOwnerRepository.findById(query.petOwnerId());

        if (petOwner.isEmpty()) return Optional.empty();

        var authenticatedUserId = iamExternalService.fetchAuthenticatedUserId();

        if (!petOwner.get().getUserId().userId().equals(authenticatedUserId)) {
            throw new ProfileOperationNotAllowedException(
                    "You cannot access another user's profile");
        }

        return petOwner;
    }

    /**
     * Retrieves a pet owner by its IAM user identifier.
     *
     * @param query the IAM user identifier query
     * @return the pet owner, if found
     */
    @Override
    public Optional<PetOwner> handle(GetPetOwnerByUserIdQuery query) {
        var authenticatedUserId = iamExternalService.fetchAuthenticatedUserId();

        if (!query.userId().equals(authenticatedUserId)) {
            throw new ProfileOperationNotAllowedException(
                    "You cannot access another user's profile");
        }
        return petOwnerRepository.findByUserId(new UserId(query.userId()));
    }
}

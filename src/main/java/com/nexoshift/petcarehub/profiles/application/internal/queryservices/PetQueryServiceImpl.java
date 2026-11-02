package com.nexoshift.petcarehub.profiles.application.internal.queryservices;

import com.nexoshift.petcarehub.profiles.application.internal.outboundservices.acl.IamExternalService;
import com.nexoshift.petcarehub.profiles.domain.exceptions.PetOwnerNotFoundException;
import com.nexoshift.petcarehub.profiles.domain.exceptions.ProfileOperationNotAllowedException;
import com.nexoshift.petcarehub.profiles.domain.model.aggregates.Pet;
import com.nexoshift.petcarehub.profiles.domain.model.queries.GetPetByIdQuery;
import com.nexoshift.petcarehub.profiles.domain.model.queries.GetPetsByOwnerIdQuery;
import com.nexoshift.petcarehub.profiles.domain.model.valueobjects.OwnerId;
import com.nexoshift.petcarehub.profiles.domain.services.PetQueryService;
import com.nexoshift.petcarehub.profiles.infrastructure.persistence.jpa.repositories.PetOwnerRepository;
import com.nexoshift.petcarehub.profiles.infrastructure.persistence.jpa.repositories.PetRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Implementation of the pet query service.
 * Handles queries related to pet profiles.
 *
 * @see PetQueryService
 * @see PetRepository
 */
@Service
public class PetQueryServiceImpl implements PetQueryService {

    private final PetRepository petRepository;
    private final PetOwnerRepository petOwnerRepository;
    private final IamExternalService iamExternalService;

    /**
     * Constructs a pet query service.
     *
     * @param petRepository the pet repository
     */
    public PetQueryServiceImpl(
            PetRepository petRepository,
            PetOwnerRepository petOwnerRepository,
            IamExternalService iamExternalService) {
        this.petRepository = petRepository;
        this.petOwnerRepository = petOwnerRepository;
        this.iamExternalService = iamExternalService;
    }

    /**
     * Retrieves a pet by its profile identifier.
     *
     * @param query the pet identifier query
     * @return the pet, if found
     */
    @Override
    public Optional<Pet> handle(GetPetByIdQuery query) {

        var pet = petRepository.findById(query.petId());

        if (pet.isEmpty()) return Optional.empty();

        var petOwner = petOwnerRepository.findById(pet.get().getOwnerId().ownerId())
                .orElseThrow(() -> new PetOwnerNotFoundException(pet.get().getOwnerId().ownerId()));

        var authenticatedUserId = iamExternalService.fetchAuthenticatedUserId();

        if (!petOwner.getUserId().userId().equals(authenticatedUserId)) {
            throw new ProfileOperationNotAllowedException(
                    "You cannot access another owner's pet");
        }
        return pet;
    }

    /**
     * Retrieves all pets belonging to a pet owner.
     *
     * @param query the pet owner identifier query
     * @return the list of pets belonging to the owner
     */
    @Override
    public List<Pet> handle(GetPetsByOwnerIdQuery query) {

        var petOwner = petOwnerRepository.findById(query.ownerId())
                .orElseThrow(() -> new PetOwnerNotFoundException(query.ownerId()));

        var authenticatedUserId = iamExternalService.fetchAuthenticatedUserId();

        if (!petOwner.getUserId().userId().equals(authenticatedUserId)) {
            throw new ProfileOperationNotAllowedException(
                    "You cannot access another owner's pets");
        }

        return petRepository.findByOwnerId(
                new OwnerId(query.ownerId())
        );
    }
}

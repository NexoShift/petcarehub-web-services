package com.nexoshift.petcarehub.profiles.application.internal.queryservices;

import com.nexoshift.petcarehub.profiles.domain.model.aggregates.Pet;
import com.nexoshift.petcarehub.profiles.domain.model.queries.GetPetByIdQuery;
import com.nexoshift.petcarehub.profiles.domain.model.queries.GetPetsByOwnerIdQuery;
import com.nexoshift.petcarehub.profiles.domain.model.valueobjects.OwnerId;
import com.nexoshift.petcarehub.profiles.domain.services.PetQueryService;
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

    /**
     * Constructs a pet query service.
     *
     * @param petRepository the pet repository
     */
    public PetQueryServiceImpl(PetRepository petRepository) {
        this.petRepository = petRepository;
    }

    /**
     * Retrieves a pet by its profile identifier.
     *
     * @param query the pet identifier query
     * @return the pet, if found
     */
    @Override
    public Optional<Pet> handle(GetPetByIdQuery query) {
        return petRepository.findById(query.petId());
    }

    /**
     * Retrieves all pets belonging to a pet owner.
     *
     * @param query the pet owner identifier query
     * @return the list of pets belonging to the owner
     */
    @Override
    public List<Pet> handle(GetPetsByOwnerIdQuery query) {
        return petRepository.findByOwnerId(
                new OwnerId(query.ownerId())
        );
    }
}

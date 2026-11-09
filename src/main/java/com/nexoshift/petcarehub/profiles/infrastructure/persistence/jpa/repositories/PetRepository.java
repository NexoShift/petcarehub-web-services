package com.nexoshift.petcarehub.profiles.infrastructure.persistence.jpa.repositories;

import com.nexoshift.petcarehub.profiles.domain.model.aggregates.Pet;
import com.nexoshift.petcarehub.profiles.domain.model.valueobjects.OwnerId;
import com.nexoshift.petcarehub.profiles.domain.model.valueobjects.PetCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for managing pet profiles.
 */
@Repository
public interface PetRepository extends JpaRepository<Pet, Long> {

    List<Pet> findByOwnerId(OwnerId ownerId);

    boolean existsByOwnerId(OwnerId ownerId);

    /**
     * Checks whether a pet code already exists.
     *
     * @param petCode the pet code
     * @return true if the pet code exists, false otherwise
     */
    boolean existsByPetCode(PetCode petCode);

    /**
     * Finds a pet by its unique code.
     *
     * @param petCode the pet code
     * @return the pet, if found
     */
    Optional<Pet> findByPetCode(PetCode petCode);
}

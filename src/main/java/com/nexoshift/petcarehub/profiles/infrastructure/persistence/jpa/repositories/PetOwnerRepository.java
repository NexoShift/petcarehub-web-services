package com.nexoshift.petcarehub.profiles.infrastructure.persistence.jpa.repositories;

import com.nexoshift.petcarehub.profiles.domain.model.aggregates.PetOwner;
import com.nexoshift.petcarehub.profiles.domain.model.valueobjects.UserId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for managing pet owner profiles.
 */
@Repository
public interface PetOwnerRepository extends JpaRepository<PetOwner, Long> {

    Optional<PetOwner> findByUserId(UserId userId);

    boolean existsByUserId(UserId userId);
}

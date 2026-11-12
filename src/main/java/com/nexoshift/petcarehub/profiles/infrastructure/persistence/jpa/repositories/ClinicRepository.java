package com.nexoshift.petcarehub.profiles.infrastructure.persistence.jpa.repositories;

import com.nexoshift.petcarehub.profiles.domain.model.aggregates.Clinic;
import com.nexoshift.petcarehub.profiles.domain.model.valueobjects.AccountId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for managing clinic profiles.
 */
@Repository
public interface ClinicRepository extends JpaRepository<Clinic, Long> {

    Optional<Clinic> findByAccountId(AccountId accountId);

    boolean existsByAccountId(AccountId accountId);
}
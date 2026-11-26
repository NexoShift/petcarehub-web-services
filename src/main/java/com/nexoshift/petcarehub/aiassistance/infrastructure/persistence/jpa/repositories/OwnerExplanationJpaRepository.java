package com.nexoshift.petcarehub.aiassistance.infrastructure.persistence.jpa.repositories;

import com.nexoshift.petcarehub.aiassistance.domain.model.aggregates.OwnerExplanation;
import com.nexoshift.petcarehub.aiassistance.domain.repositories.OwnerExplanationRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OwnerExplanationJpaRepository
    extends JpaRepository<OwnerExplanation, Long>, OwnerExplanationRepository {
}

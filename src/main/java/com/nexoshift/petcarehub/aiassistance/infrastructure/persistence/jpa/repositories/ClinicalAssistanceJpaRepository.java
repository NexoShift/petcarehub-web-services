package com.nexoshift.petcarehub.aiassistance.infrastructure.persistence.jpa.repositories;

import com.nexoshift.petcarehub.aiassistance.domain.model.aggregates.ClinicalAssistance;
import com.nexoshift.petcarehub.aiassistance.domain.repositories.ClinicalAssistanceRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/** Spring Data implements the domain port by itself, as in IAM. */
@Repository
public interface ClinicalAssistanceJpaRepository
    extends JpaRepository<ClinicalAssistance, Long>, ClinicalAssistanceRepository {
}

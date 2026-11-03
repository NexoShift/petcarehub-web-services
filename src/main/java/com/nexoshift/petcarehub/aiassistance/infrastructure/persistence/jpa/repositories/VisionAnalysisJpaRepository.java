package com.nexoshift.petcarehub.aiassistance.infrastructure.persistence.jpa.repositories;

import com.nexoshift.petcarehub.aiassistance.domain.model.aggregates.VisionAnalysis;
import com.nexoshift.petcarehub.aiassistance.domain.repositories.VisionAnalysisRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VisionAnalysisJpaRepository
    extends JpaRepository<VisionAnalysis, Long>, VisionAnalysisRepository {
}

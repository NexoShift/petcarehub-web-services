package com.nexoshift.petcarehub.aiassistance.domain.repositories;

import com.nexoshift.petcarehub.aiassistance.domain.model.aggregates.VisionAnalysis;
import com.nexoshift.petcarehub.aiassistance.domain.model.valueobjects.ClinicalConsultationId;
import com.nexoshift.petcarehub.aiassistance.domain.model.valueobjects.PetId;
import java.util.List;
import java.util.Optional;

/** Persistence port of the VisionAnalysis aggregate. Implemented in infrastructure. */
public interface VisionAnalysisRepository {

  VisionAnalysis save(VisionAnalysis visionAnalysis);

  Optional<VisionAnalysis> findById(Long id);

  List<VisionAnalysis> findByPetIdOrderByIdDesc(PetId petId);

  List<VisionAnalysis> findByConsultationIdOrderByIdDesc(ClinicalConsultationId consultationId);
}

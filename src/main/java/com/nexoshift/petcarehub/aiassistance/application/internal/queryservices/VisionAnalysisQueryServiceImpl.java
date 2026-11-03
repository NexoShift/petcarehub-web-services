package com.nexoshift.petcarehub.aiassistance.application.internal.queryservices;

import com.nexoshift.petcarehub.aiassistance.domain.model.aggregates.VisionAnalysis;
import com.nexoshift.petcarehub.aiassistance.domain.model.queries.GetVisionAnalysesByConsultationIdQuery;
import com.nexoshift.petcarehub.aiassistance.domain.model.queries.GetVisionAnalysesByPetIdQuery;
import com.nexoshift.petcarehub.aiassistance.domain.model.queries.GetVisionAnalysisByIdQuery;
import com.nexoshift.petcarehub.aiassistance.domain.model.valueobjects.ClinicalConsultationId;
import com.nexoshift.petcarehub.aiassistance.domain.model.valueobjects.PetId;
import com.nexoshift.petcarehub.aiassistance.domain.repositories.VisionAnalysisRepository;
import com.nexoshift.petcarehub.aiassistance.domain.services.VisionAnalysisQueryService;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class VisionAnalysisQueryServiceImpl implements VisionAnalysisQueryService {

  private final VisionAnalysisRepository repository;

  public VisionAnalysisQueryServiceImpl(VisionAnalysisRepository repository) {
    this.repository = repository;
  }

  @Override
  public Optional<VisionAnalysis> handle(GetVisionAnalysisByIdQuery query) {
    return repository.findById(query.visionAnalysisId());
  }

  @Override
  public List<VisionAnalysis> handle(GetVisionAnalysesByPetIdQuery query) {
    return repository.findByPetIdOrderByIdDesc(new PetId(query.petId()));
  }

  @Override
  public List<VisionAnalysis> handle(GetVisionAnalysesByConsultationIdQuery query) {
    return repository.findByConsultationIdOrderByIdDesc(
        new ClinicalConsultationId(query.consultationId()));
  }
}

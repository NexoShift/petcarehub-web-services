package com.nexoshift.petcarehub.aiassistance.application.internal.queryservices;

import com.nexoshift.petcarehub.aiassistance.domain.model.aggregates.ClinicalAssistance;
import com.nexoshift.petcarehub.aiassistance.domain.model.queries.GetClinicalAssistanceByConsultationIdQuery;
import com.nexoshift.petcarehub.aiassistance.domain.model.queries.GetClinicalAssistanceByIdQuery;
import com.nexoshift.petcarehub.aiassistance.domain.model.valueobjects.ClinicalConsultationId;
import com.nexoshift.petcarehub.aiassistance.domain.repositories.ClinicalAssistanceRepository;
import com.nexoshift.petcarehub.aiassistance.domain.services.ClinicalAssistanceQueryService;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class ClinicalAssistanceQueryServiceImpl implements ClinicalAssistanceQueryService {

  private final ClinicalAssistanceRepository repository;

  public ClinicalAssistanceQueryServiceImpl(ClinicalAssistanceRepository repository) {
    this.repository = repository;
  }

  @Override
  public Optional<ClinicalAssistance> handle(GetClinicalAssistanceByIdQuery query) {
    return repository.findById(query.clinicalAssistanceId());
  }

  @Override
  public Optional<ClinicalAssistance> handle(GetClinicalAssistanceByConsultationIdQuery query) {
    return repository.findFirstByConsultationIdOrderByIdDesc(
        new ClinicalConsultationId(query.consultationId()));
  }
}

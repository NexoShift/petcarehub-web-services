package com.nexoshift.petcarehub.clinicalattention.interfaces.acl;

import com.nexoshift.petcarehub.clinicalattention.domain.model.aggregates.ClinicalConsultation;
import com.nexoshift.petcarehub.clinicalattention.domain.model.queries.GetClinicalConsultationByIdQuery;
import com.nexoshift.petcarehub.clinicalattention.domain.services.ClinicalConsultationQueryService;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class ClinicalAttentionContextFacadeImpl implements ClinicalAttentionContextFacade {

  private final ClinicalConsultationQueryService queryService;

  public ClinicalAttentionContextFacadeImpl(ClinicalConsultationQueryService queryService) {
    this.queryService = queryService;
  }

  @Override
  public boolean existsConsultation(Long consultationId) {
    return find(consultationId).isPresent();
  }

  @Override
  public boolean isConsultationInProgress(Long consultationId) {
    return find(consultationId).map(ClinicalConsultation::isInProgress).orElse(false);
  }

  @Override
  public Long fetchPetIdByConsultationId(Long consultationId) {
    return find(consultationId).map(consultation -> consultation.getPetId().petId()).orElse(0L);
  }

  @Override
  public Long fetchVeterinarianIdByConsultationId(Long consultationId) {
    return find(consultationId)
        .map(consultation -> consultation.getVeterinarianId().veterinarianId())
        .orElse(0L);
  }

  private Optional<ClinicalConsultation> find(Long consultationId) {
    if (consultationId == null || consultationId <= 0) {
      return Optional.empty();
    }
    return queryService.handle(new GetClinicalConsultationByIdQuery(consultationId));
  }
}

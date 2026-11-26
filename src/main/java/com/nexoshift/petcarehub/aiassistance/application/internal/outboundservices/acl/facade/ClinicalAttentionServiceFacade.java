package com.nexoshift.petcarehub.aiassistance.application.internal.outboundservices.acl.facade;

import com.nexoshift.petcarehub.aiassistance.application.internal.outboundservices.acl.ConsultationBackground;
import com.nexoshift.petcarehub.aiassistance.application.internal.outboundservices.acl.ExternalClinicalAttentionService;
import com.nexoshift.petcarehub.clinicalattention.interfaces.acl.ClinicalAttentionContextFacade;
import com.nexoshift.petcarehub.clinicalattention.interfaces.acl.ConsultationSummary;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * NUEVO. Adaptador (ACL) sobre la fachada pública de Clinical Attention.
 * No lleva @ConditionalOnProperty: las consultas son siempre reales (no hay consultas demo).
 * Es el primer consumidor de ClinicalAttentionContextFacade.
 */
@Service
public class ClinicalAttentionServiceFacade implements ExternalClinicalAttentionService {

  private final ClinicalAttentionContextFacade clinicalAttentionContextFacade;

  public ClinicalAttentionServiceFacade(
      ClinicalAttentionContextFacade clinicalAttentionContextFacade) {
    this.clinicalAttentionContextFacade = clinicalAttentionContextFacade;
  }

  @Override
  public Optional<Long> fetchPetIdByConsultationId(Long consultationId) {
    var petId = clinicalAttentionContextFacade.fetchPetIdByConsultationId(consultationId);
    return petId == null || petId <= 0 ? Optional.empty() : Optional.of(petId);
  }

  @Override
  public boolean isConsultationInProgress(Long consultationId) {
    return clinicalAttentionContextFacade.isConsultationInProgress(consultationId);
  }

  @Override
  public Optional<ConsultationBackground> fetchConsultation(Long consultationId) {
    if (consultationId == null || consultationId <= 0) {
      return Optional.empty();
    }
    return clinicalAttentionContextFacade.fetchConsultationSummary(consultationId)
        .map(ClinicalAttentionServiceFacade::toBackground);
  }

  @Override
  public List<ConsultationBackground> fetchConsultationsByPetId(Long petId) {
    return clinicalAttentionContextFacade.fetchCompletedConsultationSummariesByPetId(petId).stream()
        .map(ClinicalAttentionServiceFacade::toBackground)
        .toList();
  }

  private static ConsultationBackground toBackground(ConsultationSummary summary) {
    return new ConsultationBackground(summary.consultationId(), summary.petId(),
        summary.clinicId(), summary.date(),
        summary.reason(), summary.findings(), summary.diagnosis(), summary.treatment());
  }
}

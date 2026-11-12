package com.nexoshift.petcarehub.aiassistance.application.internal.outboundservices.acl.facade;

import com.nexoshift.petcarehub.aiassistance.application.internal.outboundservices.acl.ExternalClinicalAttentionService;
import com.nexoshift.petcarehub.clinicalattention.interfaces.acl.ClinicalAttentionContextFacade;
import org.springframework.stereotype.Service;

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
}

package com.nexoshift.petcarehub.clinicalexams.infrastructure.acl;

import com.nexoshift.petcarehub.clinicalattention.interfaces.acl.ClinicalAttentionContextFacade;
import com.nexoshift.petcarehub.clinicalexams.application.internal.outboundservices.acl.ExternalClinicalAttentionService;
import org.springframework.stereotype.Service;

import java.util.Optional;

/** Adapter to the published facade of Clinical Attention (0 means "does not exist"). */
@Service
public class ClinicalAttentionExternalService implements ExternalClinicalAttentionService {

    private final ClinicalAttentionContextFacade clinicalAttentionContextFacade;

    public ClinicalAttentionExternalService(
            ClinicalAttentionContextFacade clinicalAttentionContextFacade) {
        this.clinicalAttentionContextFacade = clinicalAttentionContextFacade;
    }

    @Override
    public boolean isConsultationInProgress(Long consultationId) {
        return clinicalAttentionContextFacade.isConsultationInProgress(consultationId);
    }

    @Override
    public Optional<Long> fetchPetIdByConsultationId(Long consultationId) {
        return positive(clinicalAttentionContextFacade.fetchPetIdByConsultationId(consultationId));
    }

    @Override
    public Optional<Long> fetchClinicIdByConsultationId(Long consultationId) {
        return positive(
                clinicalAttentionContextFacade.fetchClinicIdByConsultationId(consultationId));
    }

    private static Optional<Long> positive(Long value) {
        return value != null && value > 0 ? Optional.of(value) : Optional.empty();
    }
}

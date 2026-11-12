package com.nexoshift.petcarehub.clinicalexams.application.internal.outboundservices.acl;

import java.util.Optional;

/** Outbound port to Clinical Attention: what Clinical Exams needs to know about a consultation. */
public interface ExternalClinicalAttentionService {

    boolean isConsultationInProgress(Long consultationId);

    Optional<Long> fetchPetIdByConsultationId(Long consultationId);

    Optional<Long> fetchClinicIdByConsultationId(Long consultationId);
}

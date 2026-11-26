package com.nexoshift.petcarehub.aiassistance.domain.repositories;

import com.nexoshift.petcarehub.aiassistance.domain.model.aggregates.ClinicalAssistance;
import com.nexoshift.petcarehub.aiassistance.domain.model.valueobjects.ClinicalConsultationId;
import java.util.Optional;

/** Persistence port of the ClinicalAssistance aggregate. Implemented in infrastructure. */
public interface ClinicalAssistanceRepository {

  ClinicalAssistance save(ClinicalAssistance clinicalAssistance);

  Optional<ClinicalAssistance> findById(Long id);

  Optional<ClinicalAssistance> findFirstByConsultationIdOrderByIdDesc(
      ClinicalConsultationId consultationId);
}

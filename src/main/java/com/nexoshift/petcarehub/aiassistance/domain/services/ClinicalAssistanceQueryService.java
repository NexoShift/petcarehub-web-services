package com.nexoshift.petcarehub.aiassistance.domain.services;

import com.nexoshift.petcarehub.aiassistance.domain.model.aggregates.ClinicalAssistance;
import com.nexoshift.petcarehub.aiassistance.domain.model.queries.GetClinicalAssistanceByConsultationIdQuery;
import com.nexoshift.petcarehub.aiassistance.domain.model.queries.GetClinicalAssistanceByIdQuery;
import java.util.Optional;

public interface ClinicalAssistanceQueryService {

  Optional<ClinicalAssistance> handle(GetClinicalAssistanceByIdQuery query);

  Optional<ClinicalAssistance> handle(GetClinicalAssistanceByConsultationIdQuery query);
}

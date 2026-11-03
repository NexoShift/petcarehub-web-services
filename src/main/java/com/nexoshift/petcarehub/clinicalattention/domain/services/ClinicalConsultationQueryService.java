package com.nexoshift.petcarehub.clinicalattention.domain.services;

import com.nexoshift.petcarehub.clinicalattention.domain.model.aggregates.ClinicalConsultation;
import com.nexoshift.petcarehub.clinicalattention.domain.model.queries.GetClinicalConsultationByIdQuery;
import com.nexoshift.petcarehub.clinicalattention.domain.model.queries.GetClinicalConsultationsByPetIdQuery;
import com.nexoshift.petcarehub.clinicalattention.domain.model.queries.GetClinicalConsultationsByVeterinarianIdQuery;
import java.util.List;
import java.util.Optional;

public interface ClinicalConsultationQueryService {

  Optional<ClinicalConsultation> handle(GetClinicalConsultationByIdQuery query);

  List<ClinicalConsultation> handle(GetClinicalConsultationsByPetIdQuery query);

  List<ClinicalConsultation> handle(GetClinicalConsultationsByVeterinarianIdQuery query);
}

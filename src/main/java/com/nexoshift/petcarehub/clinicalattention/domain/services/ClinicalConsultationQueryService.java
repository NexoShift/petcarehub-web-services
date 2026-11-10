package com.nexoshift.petcarehub.clinicalattention.domain.services;

import com.nexoshift.petcarehub.clinicalattention.domain.model.aggregates.ClinicalConsultation;
import com.nexoshift.petcarehub.clinicalattention.domain.model.queries.GetAuthorizedClinicalConsultationByIdQuery;
import com.nexoshift.petcarehub.clinicalattention.domain.model.queries.GetClinicalConsultationByIdQuery;
import com.nexoshift.petcarehub.clinicalattention.domain.model.queries.GetClinicalConsultationsByPetIdQuery;
import com.nexoshift.petcarehub.clinicalattention.domain.model.queries.GetClinicalConsultationsByVeterinarianIdQuery;
import java.util.List;
import java.util.Optional;

public interface ClinicalConsultationQueryService {

  /** Uso interno (fachada para otros BCs): no valida quién pregunta. */
  Optional<ClinicalConsultation> handle(GetClinicalConsultationByIdQuery query);

  /** Uso de la API REST: empty si no existe; lanza ForbiddenException si no puede verla. */
  Optional<ClinicalConsultation> handle(GetAuthorizedClinicalConsultationByIdQuery query);

  List<ClinicalConsultation> handle(GetClinicalConsultationsByPetIdQuery query);

  List<ClinicalConsultation> handle(GetClinicalConsultationsByVeterinarianIdQuery query);
}

package com.nexoshift.petcarehub.medicalhistory.domain.services;

import com.nexoshift.petcarehub.medicalhistory.domain.model.aggregates.MedicalHistory;
import com.nexoshift.petcarehub.medicalhistory.domain.model.queries.GetMedicalHistoryByPetIdQuery;

public interface MedicalHistoryQueryService {

  /** A pet without records yet gets an empty history (it is not stored). */
  MedicalHistory handle(GetMedicalHistoryByPetIdQuery query);
}

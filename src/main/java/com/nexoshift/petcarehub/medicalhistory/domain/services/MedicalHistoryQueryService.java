package com.nexoshift.petcarehub.medicalhistory.domain.services;

import com.nexoshift.petcarehub.medicalhistory.domain.model.valueobjects.VisibleMedicalHistory;
import com.nexoshift.petcarehub.medicalhistory.domain.model.queries.GetMedicalHistoryByPetIdQuery;

public interface MedicalHistoryQueryService {

  /**
   * A pet without records yet gets an empty history (it is not stored). A veterinarian sees
   * the records of other clinics only while the owner's access grant is active.
   */
  VisibleMedicalHistory handle(GetMedicalHistoryByPetIdQuery query);
}

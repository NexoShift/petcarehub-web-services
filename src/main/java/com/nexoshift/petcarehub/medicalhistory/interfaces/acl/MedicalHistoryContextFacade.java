package com.nexoshift.petcarehub.medicalhistory.interfaces.acl;

import java.util.List;

/** Published contract of Medical History for other contexts (for example AI Assistance). */
public interface MedicalHistoryContextFacade {

  /**
   * Known allergies of the pet as short texts, e.g. "Penicillin (SEVERE): hives". Empty when
   * there are none. It does not check permissions: the caller already did.
   */
  List<String> fetchAllergySummariesByPetId(Long petId);
}

package com.nexoshift.petcarehub.clinicalattention.interfaces.acl;

/**
 * Published contract (Open Host Service) of Clinical Attention. It is the only type of this
 * context that other bounded contexts may use, and it only exposes simple types.
 */
public interface ClinicalAttentionContextFacade {

  boolean existsConsultation(Long consultationId);

  boolean isConsultationInProgress(Long consultationId);

  /** Returns the pet id of the consultation, or 0 if the consultation does not exist. */
  Long fetchPetIdByConsultationId(Long consultationId);

  /** Returns the veterinarian in charge, or 0 if the consultation does not exist. */
  Long fetchVeterinarianIdByConsultationId(Long consultationId);
}

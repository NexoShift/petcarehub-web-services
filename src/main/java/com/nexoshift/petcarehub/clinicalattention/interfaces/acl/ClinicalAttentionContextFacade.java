package com.nexoshift.petcarehub.clinicalattention.interfaces.acl;

import java.util.List;
import java.util.Optional;

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

  /** Returns the clinic where the consultation takes place, or 0 if it does not exist. */
  Long fetchClinicIdByConsultationId(Long consultationId);

  /** Summary of any consultation, or empty if it does not exist. No permission check. */
  Optional<ConsultationSummary> fetchConsultationSummary(Long consultationId);

  /** Summaries of the COMPLETED consultations of a pet, newest first. No permission check. */
  List<ConsultationSummary> fetchCompletedConsultationSummariesByPetId(Long petId);
}

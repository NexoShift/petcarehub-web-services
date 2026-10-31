package com.nexoshift.petcarehub.clinicalattention.domain.repositories;

import com.nexoshift.petcarehub.clinicalattention.domain.model.aggregates.ClinicalConsultation;
import com.nexoshift.petcarehub.clinicalattention.domain.model.valueobjects.PetId;
import com.nexoshift.petcarehub.clinicalattention.domain.model.valueobjects.VeterinarianId;
import java.util.List;
import java.util.Optional;

/** Persistence port of the ClinicalConsultation aggregate. Implemented in infrastructure. */
public interface ClinicalConsultationRepository {

  ClinicalConsultation save(ClinicalConsultation consultation);

  Optional<ClinicalConsultation> findById(Long id);

  /** Newest first. */
  List<ClinicalConsultation> findAllByPetIdOrderByCreatedAtDesc(PetId petId);

  /** Newest first. */
  List<ClinicalConsultation> findAllByVeterinarianIdOrderByCreatedAtDesc(
      VeterinarianId veterinarianId);
}

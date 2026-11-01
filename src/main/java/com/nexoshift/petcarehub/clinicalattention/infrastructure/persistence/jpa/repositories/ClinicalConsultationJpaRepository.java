package com.nexoshift.petcarehub.clinicalattention.infrastructure.persistence.jpa.repositories;

import com.nexoshift.petcarehub.clinicalattention.domain.model.aggregates.ClinicalConsultation;
import com.nexoshift.petcarehub.clinicalattention.domain.model.valueobjects.PetId;
import com.nexoshift.petcarehub.clinicalattention.domain.model.valueobjects.VeterinarianId;
import com.nexoshift.petcarehub.clinicalattention.domain.repositories.ClinicalConsultationRepository;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data adapter of the ClinicalConsultationRepository port. The derived queries filter by
 * the number inside the id value object ({@code petId.petId}).
 */
@Repository
public interface ClinicalConsultationJpaRepository
    extends JpaRepository<ClinicalConsultation, Long>, ClinicalConsultationRepository {

  List<ClinicalConsultation> findAllByPetId_PetIdOrderByCreatedAtDesc(Long petId);

  List<ClinicalConsultation> findAllByVeterinarianId_VeterinarianIdOrderByCreatedAtDesc(
      Long veterinarianId);

  @Override
  default List<ClinicalConsultation> findAllByPetIdOrderByCreatedAtDesc(PetId petId) {
    return findAllByPetId_PetIdOrderByCreatedAtDesc(petId.petId());
  }

  @Override
  default List<ClinicalConsultation> findAllByVeterinarianIdOrderByCreatedAtDesc(
      VeterinarianId veterinarianId) {
    return findAllByVeterinarianId_VeterinarianIdOrderByCreatedAtDesc(
        veterinarianId.veterinarianId());
  }
}

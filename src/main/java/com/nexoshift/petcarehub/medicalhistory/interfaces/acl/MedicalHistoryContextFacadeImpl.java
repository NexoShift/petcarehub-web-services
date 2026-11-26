package com.nexoshift.petcarehub.medicalhistory.interfaces.acl;

import com.nexoshift.petcarehub.medicalhistory.infrastructure.persistence.jpa.repositories.MedicalHistoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class MedicalHistoryContextFacadeImpl implements MedicalHistoryContextFacade {

  private final MedicalHistoryRepository repository;

  public MedicalHistoryContextFacadeImpl(MedicalHistoryRepository repository) {
    this.repository = repository;
  }

  @Override
  @Transactional(readOnly = true)
  public List<String> fetchAllergySummariesByPetId(Long petId) {
    return repository.findByPetId(petId)
        .map(history -> history.getAllergies().stream()
            .map(allergy -> allergy.getAllergen() + " (" + allergy.getSeverity() + ")"
                + (allergy.getReaction() == null ? "" : ": " + allergy.getReaction()))
            .toList())
        .orElse(List.of());
  }
}

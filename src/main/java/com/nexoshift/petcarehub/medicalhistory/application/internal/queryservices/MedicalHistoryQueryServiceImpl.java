package com.nexoshift.petcarehub.medicalhistory.application.internal.queryservices;

import com.nexoshift.petcarehub.medicalhistory.application.internal.access.MedicalHistoryAccessGuard;
import com.nexoshift.petcarehub.medicalhistory.domain.model.aggregates.MedicalHistory;
import com.nexoshift.petcarehub.medicalhistory.domain.model.queries.GetMedicalHistoryByPetIdQuery;
import com.nexoshift.petcarehub.medicalhistory.domain.model.valueobjects.VisibleMedicalHistory;
import com.nexoshift.petcarehub.medicalhistory.domain.services.MedicalHistoryQueryService;
import com.nexoshift.petcarehub.medicalhistory.infrastructure.persistence.jpa.repositories.MedicalHistoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MedicalHistoryQueryServiceImpl implements MedicalHistoryQueryService {

  private final MedicalHistoryRepository repository;
  private final MedicalHistoryAccessGuard accessGuard;

  public MedicalHistoryQueryServiceImpl(MedicalHistoryRepository repository,
                                        MedicalHistoryAccessGuard accessGuard) {
    this.repository = repository;
    this.accessGuard = accessGuard;
  }

  @Override
  @Transactional(readOnly = true)
  public VisibleMedicalHistory handle(GetMedicalHistoryByPetIdQuery query) {
    var canSeeClinic = accessGuard.requireVisibleClinics(query.requesterUserId(), query.petId());
    return repository.findByPetId(query.petId())
        .orElseGet(() -> new MedicalHistory(query.petId()))
        .visibleTo(canSeeClinic);
  }
}

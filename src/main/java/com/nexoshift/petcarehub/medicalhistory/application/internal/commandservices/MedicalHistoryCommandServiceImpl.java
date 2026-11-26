package com.nexoshift.petcarehub.medicalhistory.application.internal.commandservices;

import com.nexoshift.petcarehub.medicalhistory.application.internal.access.MedicalHistoryAccessGuard;
import com.nexoshift.petcarehub.medicalhistory.domain.model.aggregates.MedicalHistory;
import com.nexoshift.petcarehub.medicalhistory.domain.model.commands.RecordAllergyCommand;
import com.nexoshift.petcarehub.medicalhistory.domain.model.commands.RecordConsultationCompletedCommand;
import com.nexoshift.petcarehub.medicalhistory.domain.model.commands.RecordExamResultValidatedCommand;
import com.nexoshift.petcarehub.medicalhistory.domain.model.commands.RecordVaccinationCommand;
import com.nexoshift.petcarehub.medicalhistory.domain.services.MedicalHistoryCommandService;
import com.nexoshift.petcarehub.medicalhistory.infrastructure.persistence.jpa.repositories.MedicalHistoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MedicalHistoryCommandServiceImpl implements MedicalHistoryCommandService {

  private final MedicalHistoryRepository repository;
  private final MedicalHistoryAccessGuard accessGuard;

  public MedicalHistoryCommandServiceImpl(MedicalHistoryRepository repository,
                                          MedicalHistoryAccessGuard accessGuard) {
    this.repository = repository;
    this.accessGuard = accessGuard;
  }

  @Override
  @Transactional
  public MedicalHistory handle(RecordAllergyCommand command) {
    var reporter = accessGuard.requireCanAccess(command.requesterUserId(), command.petId());
    var history = findOrCreate(command.petId());
    history.recordAllergy(command, reporter);
    return repository.save(history);
  }

  @Override
  @Transactional
  public MedicalHistory handle(RecordVaccinationCommand command) {
    var veterinarian = accessGuard.requireTreatingVeterinarian(
        command.requesterUserId(), command.petId());
    var history = findOrCreate(command.petId());
    history.recordVaccination(command, veterinarian.veterinarianId(), veterinarian.clinicId());
    return repository.save(history);
  }

  @Override
  @Transactional
  public void handle(RecordConsultationCompletedCommand command) {
    var history = findOrCreate(command.petId());
    history.recordConsultationCompleted(command);
    repository.save(history);
  }

  @Override
  @Transactional
  public void handle(RecordExamResultValidatedCommand command) {
    var history = findOrCreate(command.petId());
    history.recordExamResultValidated(command);
    repository.save(history);
  }

  private MedicalHistory findOrCreate(Long petId) {
    return repository.findByPetId(petId).orElseGet(() -> new MedicalHistory(petId));
  }
}

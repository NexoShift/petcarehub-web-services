package com.nexoshift.petcarehub.medicalhistory.domain.services;

import com.nexoshift.petcarehub.medicalhistory.domain.model.aggregates.MedicalHistory;
import com.nexoshift.petcarehub.medicalhistory.domain.model.commands.RecordAllergyCommand;
import com.nexoshift.petcarehub.medicalhistory.domain.model.commands.RecordConsultationCompletedCommand;
import com.nexoshift.petcarehub.medicalhistory.domain.model.commands.RecordExamResultValidatedCommand;
import com.nexoshift.petcarehub.medicalhistory.domain.model.commands.RecordVaccinationCommand;

public interface MedicalHistoryCommandService {

  MedicalHistory handle(RecordAllergyCommand command);

  MedicalHistory handle(RecordVaccinationCommand command);

  void handle(RecordConsultationCompletedCommand command);

  void handle(RecordExamResultValidatedCommand command);
}

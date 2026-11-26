package com.nexoshift.petcarehub.medicalhistory.application.internal.eventhandlers;

import com.nexoshift.petcarehub.clinicalexams.interfaces.acl.events.ExamResultValidatedIntegrationEvent;
import com.nexoshift.petcarehub.medicalhistory.domain.model.commands.RecordExamResultValidatedCommand;
import com.nexoshift.petcarehub.medicalhistory.domain.services.MedicalHistoryCommandService;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

/** Adds every validated exam result to the longitudinal record of the pet. */
@Service("medicalHistoryExamResultValidatedEventHandler")
public class ExamResultValidatedEventHandler {

  private final MedicalHistoryCommandService commandService;

  public ExamResultValidatedEventHandler(MedicalHistoryCommandService commandService) {
    this.commandService = commandService;
  }

  @EventListener
  public void on(ExamResultValidatedIntegrationEvent event) {
    commandService.handle(new RecordExamResultValidatedCommand(event.examId(), event.petId(),
        event.clinicId(), event.consultationId(), event.examType(), event.resultSummary(),
        event.validatedAt()));
  }
}

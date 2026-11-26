package com.nexoshift.petcarehub.medicalhistory.application.internal.eventhandlers;

import com.nexoshift.petcarehub.clinicalattention.interfaces.acl.events.ClinicalConsultationCompletedIntegrationEvent;
import com.nexoshift.petcarehub.medicalhistory.domain.model.commands.RecordConsultationCompletedCommand;
import com.nexoshift.petcarehub.medicalhistory.domain.services.MedicalHistoryCommandService;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

/** Adds every completed consultation to the longitudinal record of the pet. */
@Service("medicalHistoryConsultationCompletedEventHandler")
public class ClinicalConsultationCompletedEventHandler {

  private final MedicalHistoryCommandService commandService;

  public ClinicalConsultationCompletedEventHandler(MedicalHistoryCommandService commandService) {
    this.commandService = commandService;
  }

  @EventListener
  public void on(ClinicalConsultationCompletedIntegrationEvent event) {
    commandService.handle(new RecordConsultationCompletedCommand(event.consultationId(),
        event.petId(), event.clinicId(), event.veterinarianId(), event.primaryDiagnosis(),
        event.completedAt()));
  }
}

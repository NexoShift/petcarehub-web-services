package com.nexoshift.petcarehub.clinicalattention.application.internal.eventhandlers;

import com.nexoshift.petcarehub.clinicalattention.domain.model.events.ClinicalConsultationCompletedEvent;
import com.nexoshift.petcarehub.clinicalattention.interfaces.acl.events.ClinicalConsultationCompletedIntegrationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

/**
 * Re-publishes the internal domain event as the integration event that other bounded contexts
 * (for example Medical History) can listen to without importing Clinical Attention internals.
 */
@Service
public class ClinicalConsultationCompletedEventHandler {

  private final ApplicationEventPublisher eventPublisher;

  public ClinicalConsultationCompletedEventHandler(ApplicationEventPublisher eventPublisher) {
    this.eventPublisher = eventPublisher;
  }

  @EventListener
  public void on(ClinicalConsultationCompletedEvent event) {
    eventPublisher.publishEvent(new ClinicalConsultationCompletedIntegrationEvent(
        event.consultationId(), event.petId(), event.veterinarianId(), event.primaryDiagnosis(),
        event.completedAt()));
  }
}

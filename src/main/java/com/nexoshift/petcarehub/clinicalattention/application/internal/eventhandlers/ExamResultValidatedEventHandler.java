package com.nexoshift.petcarehub.clinicalattention.application.internal.eventhandlers;

import com.nexoshift.petcarehub.clinicalexams.domain.model.events.ExamResultValidatedEvent;
import com.nexoshift.petcarehub.clinicalexams.interfaces.acl.events.ExamResultValidatedIntegrationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

/**
 * Re-publishes the internal domain event as the integration event that other bounded contexts
 * can listen to without importing Clinical Exams internals.
 */
@Service
public class ExamResultValidatedEventHandler {

  private final ApplicationEventPublisher eventPublisher;

  public ExamResultValidatedEventHandler(ApplicationEventPublisher eventPublisher) {
    this.eventPublisher = eventPublisher;
  }

  @EventListener
  public void on(ExamResultValidatedEvent event) {
    eventPublisher.publishEvent(new ExamResultValidatedIntegrationEvent(
        event.examId(), event.petId(), event.clinicId(), event.consultationId(),
        event.examType(), event.resultSummary(), event.validatedAt()));
  }
}

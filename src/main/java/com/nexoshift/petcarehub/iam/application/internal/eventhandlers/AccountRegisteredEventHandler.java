package com.nexoshift.petcarehub.iam.application.internal.eventhandlers;

import com.nexoshift.petcarehub.iam.domain.model.events.AccountRegisteredEvent;
import com.nexoshift.petcarehub.iam.interfaces.acl.events.AccountRegisteredIntegrationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

/**
 * Re-publishes the internal domain event as the integration event that other
 * bounded contexts can listen to without importing IAM internals.
 */
@Service
public class AccountRegisteredEventHandler {

  private final ApplicationEventPublisher eventPublisher;

  public AccountRegisteredEventHandler(ApplicationEventPublisher eventPublisher) {
    this.eventPublisher = eventPublisher;
  }

  @EventListener
  public void on(AccountRegisteredEvent event) {
    eventPublisher.publishEvent(new AccountRegisteredIntegrationEvent(
        event.accountId(), event.accountType().name(), event.holderUserId(), event.occurredAt()));
  }
}

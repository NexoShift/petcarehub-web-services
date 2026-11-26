package com.nexoshift.petcarehub.aiassistance.application.internal.outboundservices.acl.facade;

import com.nexoshift.petcarehub.aiassistance.application.internal.outboundservices.acl.ExternalSubscriptionsService;
import com.nexoshift.petcarehub.subscriptions.interfaces.acl.SubscriptionContextFacade;
import org.springframework.stereotype.Service;

/**
 * Real AI credits from the Subscriptions context (ACL).
 */
@Service
public class SubscriptionsServiceFacade implements ExternalSubscriptionsService {

  private final SubscriptionContextFacade subscriptionContextFacade;

  public SubscriptionsServiceFacade(SubscriptionContextFacade subscriptionContextFacade) {
    this.subscriptionContextFacade = subscriptionContextFacade;
  }

  @Override
  public Long reserveComputerVisionCredit(Long requesterUserId) {
    return subscriptionContextFacade.reserveComputerVisionCredit(requesterUserId);
  }

  @Override
  public Long reserveLlmCredit(Long requesterUserId) {
    return subscriptionContextFacade.reserveLlmCredit(requesterUserId);
  }

  @Override
  public void confirmCredit(Long reservationId) {
    subscriptionContextFacade.confirmAiCreditReservation(reservationId);
  }

  @Override
  public void releaseCredit(Long reservationId) {
    subscriptionContextFacade.releaseAiCreditReservation(reservationId);
  }
}

package com.nexoshift.petcarehub.aiassistance.application.internal.outboundservices.acl.stub;

import com.nexoshift.petcarehub.aiassistance.application.internal.outboundservices.acl.ExternalSubscriptionsService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

/** Stub mode (ai.acl.mode=stub): AI usage is not limited. Only for isolated demos. */
@Service
@ConditionalOnProperty(name = "ai.acl.mode", havingValue = "stub", matchIfMissing = true)
public class SubscriptionsServiceStub implements ExternalSubscriptionsService {

  private static final Long NO_RESERVATION = 0L;

  @Override
  public Long reserveComputerVisionCredit(Long requesterUserId) {
    return NO_RESERVATION;
  }

  @Override
  public Long reserveLlmCredit(Long requesterUserId) {
    return NO_RESERVATION;
  }

  @Override
  public void confirmCredit(Long reservationId) {
    // nothing to confirm
  }

  @Override
  public void releaseCredit(Long reservationId) {
    // nothing to release
  }
}

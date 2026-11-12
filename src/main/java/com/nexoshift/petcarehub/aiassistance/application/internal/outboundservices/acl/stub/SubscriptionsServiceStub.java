package com.nexoshift.petcarehub.aiassistance.application.internal.outboundservices.acl.stub;

import com.nexoshift.petcarehub.aiassistance.application.internal.outboundservices.acl.ExternalSubscriptionsService;
import org.springframework.stereotype.Service;

/** Sprint 1: AI usage is not limited yet (EP05 arrives in Sprint 2). */
@Service
public class SubscriptionsServiceStub implements ExternalSubscriptionsService {

  @Override
  public void ensureAiUsageAllowed(Long veterinarianId) {
    // always allowed
  }
}

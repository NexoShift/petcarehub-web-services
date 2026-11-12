package com.nexoshift.petcarehub.aiassistance.application.internal.outboundservices.acl;

/** Sprint 1: always allows. Sprint 2: checks the AI usage limit of the clinic's plan. */
public interface ExternalSubscriptionsService {

  void ensureAiUsageAllowed(Long veterinarianId);
}

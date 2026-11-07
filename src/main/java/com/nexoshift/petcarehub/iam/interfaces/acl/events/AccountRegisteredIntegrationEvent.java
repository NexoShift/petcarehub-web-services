package com.nexoshift.petcarehub.iam.interfaces.acl.events;

import java.time.Instant;

/**
 * Published contract: other contexts (e.g. Subscriptions, to start the free plan) can listen to
 * this event. It carries no personal data: profiles are created by Profiles in onboarding step 2.
 */
public record AccountRegisteredIntegrationEvent(
        Long accountId, String accountType, Long holderUserId, Instant occurredAt) {
}

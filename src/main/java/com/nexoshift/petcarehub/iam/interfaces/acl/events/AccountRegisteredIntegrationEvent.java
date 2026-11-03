package com.nexoshift.petcarehub.iam.interfaces.acl.events;

import java.time.Instant;

/** Published contract: other contexts (e.g. Subscriptions) listen to this event. */
public record AccountRegisteredIntegrationEvent(Long accountId, String accountType,
                                                Long holderUserId, String email, String firstName, String lastName,
                                                String phoneNumber, String clinicName, String ruc, Instant occurredAt) {
}
package com.nexoshift.petcarehub.iam.domain.model.events;

import com.nexoshift.petcarehub.iam.domain.model.valueobjects.AccountType;
import java.time.Instant;

/** Internal domain event: a new account and its holder were registered. */
public record AccountRegisteredEvent(
        Long accountId, AccountType accountType, Long holderUserId, Instant occurredAt) {
}

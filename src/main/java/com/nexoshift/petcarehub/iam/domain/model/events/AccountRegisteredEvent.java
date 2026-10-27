package com.nexoshift.petcarehub.iam.domain.model.events;

import com.nexoshift.petcarehub.iam.domain.model.valueobjects.AccountType;
import java.time.Instant;

public record AccountRegisteredEvent(
    Long accountId, AccountType accountType, Long holderUserId, Instant occurredAt) {
}

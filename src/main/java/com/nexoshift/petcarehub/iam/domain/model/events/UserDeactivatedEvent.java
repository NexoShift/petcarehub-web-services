package com.nexoshift.petcarehub.iam.domain.model.events;

import java.time.Instant;

public record UserDeactivatedEvent(Long userId, Long accountId, Instant occurredAt) {
}

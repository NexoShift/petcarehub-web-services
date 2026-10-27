package com.nexoshift.petcarehub.iam.domain.model.events;

import java.time.Instant;

public record UserRegisteredEvent(Long userId, Long accountId, String email, Instant occurredAt) {
}

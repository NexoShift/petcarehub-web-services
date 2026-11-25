package com.nexoshift.petcarehub.subscriptions.domain.model.commands;

import com.nexoshift.petcarehub.subscriptions.domain.model.valueobjects.AiCreditType;

public record ReserveAiCreditCommand(
        Long requesterUserId,
        AiCreditType creditType
) {

    public ReserveAiCreditCommand {

        if (requesterUserId == null || requesterUserId <= 0) {
            throw new IllegalArgumentException(
                    "Requester user ID must be positive");
        }

        if (creditType == null) {
            throw new IllegalArgumentException(
                    "AI credit type is required");
        }
    }
}
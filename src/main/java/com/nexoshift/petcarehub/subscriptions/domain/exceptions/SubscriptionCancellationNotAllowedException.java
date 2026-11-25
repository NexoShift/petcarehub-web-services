package com.nexoshift.petcarehub.subscriptions.domain.exceptions;

import com.nexoshift.petcarehub.shared.domain.exceptions.ConflictException;

public class SubscriptionCancellationNotAllowedException
        extends ConflictException {

    public SubscriptionCancellationNotAllowedException(String message) {
        super(message);
    }
}
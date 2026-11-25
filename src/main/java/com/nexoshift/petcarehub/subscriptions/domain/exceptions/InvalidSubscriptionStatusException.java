package com.nexoshift.petcarehub.subscriptions.domain.exceptions;

import com.nexoshift.petcarehub.shared.domain.exceptions.ConflictException;

public class InvalidSubscriptionStatusException
        extends ConflictException {

    public InvalidSubscriptionStatusException() {
        super("Checkout can only be created for a pending subscription");
    }
}
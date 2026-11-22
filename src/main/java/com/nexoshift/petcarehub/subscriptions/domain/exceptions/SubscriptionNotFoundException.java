package com.nexoshift.petcarehub.subscriptions.domain.exceptions;

import com.nexoshift.petcarehub.shared.domain.exceptions.NotFoundException;

public class SubscriptionNotFoundException extends NotFoundException {

    public SubscriptionNotFoundException(Long accountId) {
        super("Subscription for account " + accountId + " was not found");
    }
}
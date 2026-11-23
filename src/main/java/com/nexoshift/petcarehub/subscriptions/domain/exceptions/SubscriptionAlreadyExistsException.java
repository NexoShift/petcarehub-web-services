package com.nexoshift.petcarehub.subscriptions.domain.exceptions;

import com.nexoshift.petcarehub.shared.domain.exceptions.ConflictException;

public class SubscriptionAlreadyExistsException extends ConflictException {

    public SubscriptionAlreadyExistsException(Long accountId) {
        super("Account " + accountId
                + " already has a subscription or Checkout process in progress");
    }
}
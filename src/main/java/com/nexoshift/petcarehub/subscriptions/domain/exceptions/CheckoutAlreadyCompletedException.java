package com.nexoshift.petcarehub.subscriptions.domain.exceptions;

import com.nexoshift.petcarehub.shared.domain.exceptions.ConflictException;

public class CheckoutAlreadyCompletedException
        extends ConflictException {

    public CheckoutAlreadyCompletedException() {
        super("Checkout has already been completed");
    }
}
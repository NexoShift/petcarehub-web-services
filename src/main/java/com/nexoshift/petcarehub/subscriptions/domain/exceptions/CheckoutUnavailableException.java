package com.nexoshift.petcarehub.subscriptions.domain.exceptions;

import com.nexoshift.petcarehub.shared.domain.exceptions.ServiceUnavailableException;

public class CheckoutUnavailableException extends ServiceUnavailableException {

    public CheckoutUnavailableException() {
        super("Checkout is temporarily unavailable");
    }

    public CheckoutUnavailableException(Throwable cause) {
        super("Checkout is temporarily unavailable", cause);
    }
}
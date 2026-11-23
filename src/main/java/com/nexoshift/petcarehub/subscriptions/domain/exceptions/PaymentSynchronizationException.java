package com.nexoshift.petcarehub.subscriptions.domain.exceptions;

import com.nexoshift.petcarehub.shared.domain.exceptions.ServiceUnavailableException;

public class PaymentSynchronizationException
        extends ServiceUnavailableException {

    public PaymentSynchronizationException() {
        super("Unable to synchronize payment information");
    }

    public PaymentSynchronizationException(Throwable cause) {
        super("Unable to synchronize payment information", cause);
    }
}
package com.nexoshift.petcarehub.subscriptions.domain.exceptions;

import com.nexoshift.petcarehub.shared.domain.exceptions.ServiceUnavailableException;

public class PaymentHistoryUnavailableException
        extends ServiceUnavailableException {

    public PaymentHistoryUnavailableException(Throwable cause) {
        super("Unable to retrieve the subscription payment history.", cause);
    }
}
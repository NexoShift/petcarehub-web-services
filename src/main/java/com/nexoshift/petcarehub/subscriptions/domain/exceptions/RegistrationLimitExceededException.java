package com.nexoshift.petcarehub.subscriptions.domain.exceptions;

import com.nexoshift.petcarehub.shared.domain.exceptions.ConflictException;

public class RegistrationLimitExceededException extends ConflictException {

    public RegistrationLimitExceededException(
            String resource,
            long registered,
            int limit) {

        super("The maximum number of " + resource
                + " allowed by the subscription plan has been reached. "
                + "Registered: " + registered + ". Limit: " + limit + ".");
    }
}
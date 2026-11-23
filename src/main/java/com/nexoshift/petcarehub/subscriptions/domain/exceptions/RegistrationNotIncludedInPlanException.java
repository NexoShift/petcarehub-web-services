package com.nexoshift.petcarehub.subscriptions.domain.exceptions;

import com.nexoshift.petcarehub.shared.domain.exceptions.ForbiddenException;

public class RegistrationNotIncludedInPlanException
        extends ForbiddenException {

    public RegistrationNotIncludedInPlanException(String resource) {
        super("The current subscription plan does not include "
                + resource + " registration.");
    }
}
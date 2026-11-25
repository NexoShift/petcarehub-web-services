package com.nexoshift.petcarehub.subscriptions.domain.exceptions;

import com.nexoshift.petcarehub.shared.domain.exceptions.ConflictException;

public class PlanNotAvailableException extends ConflictException {

    public PlanNotAvailableException(Long planId) {
        super("Subscription plan " + planId + " is not available");
    }
}
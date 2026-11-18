package com.nexoshift.petcarehub.subscriptions.domain.exceptions;

import com.nexoshift.petcarehub.shared.domain.exceptions.NotFoundException;

/**
 * Exception thrown when a subscription plan cannot be found.
 */
public class PlanNotFoundException extends NotFoundException {

    public PlanNotFoundException(Long planId) {
        super("Subscription plan with ID " + planId + " was not found.");
    }
}
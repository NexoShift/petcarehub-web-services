package com.nexoshift.petcarehub.subscriptions.domain.exceptions;

import com.nexoshift.petcarehub.shared.domain.exceptions.ForbiddenException;

public class SubscriptionAccessDeniedException extends ForbiddenException {

    public SubscriptionAccessDeniedException() {
        super("Only the active account holder can manage this subscription");
    }
}
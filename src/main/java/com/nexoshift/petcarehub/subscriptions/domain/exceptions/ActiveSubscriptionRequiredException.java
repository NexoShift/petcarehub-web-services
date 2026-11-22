package com.nexoshift.petcarehub.subscriptions.domain.exceptions;

import com.nexoshift.petcarehub.shared.domain.exceptions.ForbiddenException;

public class ActiveSubscriptionRequiredException
        extends ForbiddenException {

    public ActiveSubscriptionRequiredException() {
        super("An active subscription is required");
    }
}
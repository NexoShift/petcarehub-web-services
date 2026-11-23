package com.nexoshift.petcarehub.subscriptions.domain.exceptions;

import com.nexoshift.petcarehub.shared.domain.exceptions.ConflictException;

public class InvalidAiCreditReservationStateException
        extends ConflictException {

    public InvalidAiCreditReservationStateException() {
        super("Invalid AI credit reservation state");
    }
}
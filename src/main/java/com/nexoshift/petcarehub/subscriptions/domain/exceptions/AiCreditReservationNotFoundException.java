package com.nexoshift.petcarehub.subscriptions.domain.exceptions;

import com.nexoshift.petcarehub.shared.domain.exceptions.NotFoundException;

public class AiCreditReservationNotFoundException
        extends NotFoundException {

    public AiCreditReservationNotFoundException(Long reservationId) {
        super("AI credit reservation with ID "
                + reservationId + " was not found");
    }
}
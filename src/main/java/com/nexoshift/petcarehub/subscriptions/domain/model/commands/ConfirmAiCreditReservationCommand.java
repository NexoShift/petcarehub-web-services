package com.nexoshift.petcarehub.subscriptions.domain.model.commands;

public record ConfirmAiCreditReservationCommand(
        Long reservationId
) {

    public ConfirmAiCreditReservationCommand {

        if (reservationId == null || reservationId <= 0) {
            throw new IllegalArgumentException(
                    "Reservation ID must be positive");
        }
    }
}
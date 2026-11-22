package com.nexoshift.petcarehub.subscriptions.domain.services;

import com.nexoshift.petcarehub.subscriptions.domain.model.commands.InitializeAiCreditPeriodCommand;
import com.nexoshift.petcarehub.subscriptions.domain.model.commands.ReserveAiCreditCommand;
import com.nexoshift.petcarehub.subscriptions.domain.model.commands.ConfirmAiCreditReservationCommand;
import com.nexoshift.petcarehub.subscriptions.domain.model.commands.ReleaseAiCreditReservationCommand;

public interface AiCreditCommandService {

    void handle(InitializeAiCreditPeriodCommand command);

    Long handle(ReserveAiCreditCommand command);

    void handle(ConfirmAiCreditReservationCommand command);

    void handle(ReleaseAiCreditReservationCommand command);
}
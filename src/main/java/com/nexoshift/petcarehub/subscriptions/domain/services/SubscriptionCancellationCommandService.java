package com.nexoshift.petcarehub.subscriptions.domain.services;

import com.nexoshift.petcarehub.subscriptions.domain.model.commands
        .UpdateSubscriptionCancellationCommand;

public interface SubscriptionCancellationCommandService {

    void handle(UpdateSubscriptionCancellationCommand command);
}
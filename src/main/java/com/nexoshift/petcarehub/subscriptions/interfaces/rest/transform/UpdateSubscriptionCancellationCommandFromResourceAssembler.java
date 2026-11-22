package com.nexoshift.petcarehub.subscriptions.interfaces.rest.transform;

import com.nexoshift.petcarehub.subscriptions.domain.model.commands
        .UpdateSubscriptionCancellationCommand;
import com.nexoshift.petcarehub.subscriptions.interfaces.rest.resources
        .UpdateSubscriptionCancellationResource;

public class UpdateSubscriptionCancellationCommandFromResourceAssembler {

    public static UpdateSubscriptionCancellationCommand
    toCommandFromResource(
            Long accountId,
            UpdateSubscriptionCancellationResource resource) {

        return new UpdateSubscriptionCancellationCommand(
                accountId,
                resource.cancelAtPeriodEnd()
        );
    }
}
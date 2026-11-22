package com.nexoshift.petcarehub.subscriptions.interfaces.rest.transform;

import com.nexoshift.petcarehub.subscriptions.domain.model.commands.CreateSubscriptionCommand;
import com.nexoshift.petcarehub.subscriptions.interfaces.rest.resources.CreateSubscriptionResource;

/**
 * Assembler for creating subscription commands from resources.
 */
public class CreateSubscriptionCommandFromResourceAssembler {

    public static CreateSubscriptionCommand toCommandFromResource(
            Long accountId,
            CreateSubscriptionResource resource) {

        return new CreateSubscriptionCommand(
                accountId,
                resource.planId()
        );
    }
}
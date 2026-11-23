package com.nexoshift.petcarehub.subscriptions.domain.services;

import com.nexoshift.petcarehub.subscriptions.domain.model.aggregates.Subscription;
import com.nexoshift.petcarehub.subscriptions.domain.model.commands.CreateCheckoutSessionCommand;
import com.nexoshift.petcarehub.subscriptions.domain.model.commands.CreateSubscriptionCommand;

/**
 * Service interface for handling subscription commands.
 */
public interface SubscriptionCommandService {

    /**
     * Creates a pending subscription.
     *
     * @param command the create subscription command
     * @return the created subscription identifier
     */
    Long handle(CreateSubscriptionCommand command);

    Subscription handle(CreateCheckoutSessionCommand command);
}
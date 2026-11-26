package com.nexoshift.petcarehub.subscriptions.domain.services;

import com.nexoshift.petcarehub.subscriptions.domain.model.aggregates.Subscription;
import com.nexoshift.petcarehub.subscriptions.domain.model.commands.SimulateSubscriptionPaymentCommand;

/**
 * Simulates the Stripe payment webhook. Only exists when
 * subscriptions.simulated-payments.enabled=true (never in production).
 */
public interface SimulatedPaymentCommandService {

    Subscription handle(SimulateSubscriptionPaymentCommand command);
}

package com.nexoshift.petcarehub.subscriptions.application.internal.outboundservices.payments;

import com.nexoshift.petcarehub.subscriptions.domain.model.aggregates.Plan;
import com.nexoshift.petcarehub.subscriptions.domain.model.valueobjects.SubscriptionPaymentPage;

public interface PaymentGateway {

    CheckoutSessionData createCheckoutSession(
            Long subscriptionId,
            Long accountId,
            Plan plan,
            String idempotencyKey
    );

    CheckoutSessionData retrieveCheckoutSession(
            String sessionId
    );

    void updateSubscriptionCancellation(
            String stripeSubscriptionId,
            boolean cancelAtPeriodEnd
    );

    SubscriptionPaymentPage retrieveSubscriptionPayments(
            String stripeSubscriptionId,
            int limit,
            String startingAfter
    );
}
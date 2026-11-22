package com.nexoshift.petcarehub.subscriptions.application.internal.outboundservices.payments;

import java.util.Optional;

public interface PaymentWebhookGateway {

    Optional<PaymentWebhookData> verifyAndResolve(
            String payload,
            String signature
    );

    SubscriptionPaymentData retrieveSubscriptionPaymentData(
            String stripeSubscriptionId,
            String invoiceId
    );

    boolean checkoutBelongsToSubscription(
            String checkoutSessionId,
            String stripeSubscriptionId
    );
}
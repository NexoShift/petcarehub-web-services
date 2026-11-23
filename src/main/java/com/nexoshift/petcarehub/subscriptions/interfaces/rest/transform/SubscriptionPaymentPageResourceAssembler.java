package com.nexoshift.petcarehub.subscriptions.interfaces.rest.transform;

import com.nexoshift.petcarehub.subscriptions.domain.model.valueobjects
        .SubscriptionPayment;
import com.nexoshift.petcarehub.subscriptions.domain.model.valueobjects
        .SubscriptionPaymentPage;
import com.nexoshift.petcarehub.subscriptions.interfaces.rest.resources
        .SubscriptionPaymentResource;
import com.nexoshift.petcarehub.subscriptions.interfaces.rest.resources
        .SubscriptionPaymentPageResource;

public class SubscriptionPaymentPageResourceAssembler {

    public static SubscriptionPaymentPageResource toResource(
            SubscriptionPaymentPage page) {

        var payments = page.payments()
                .stream()
                .map(SubscriptionPaymentPageResourceAssembler::toPaymentResource)
                .toList();

        return new SubscriptionPaymentPageResource(
                payments,
                page.hasMore(),
                page.nextCursor()
        );
    }

    private static SubscriptionPaymentResource toPaymentResource(
            SubscriptionPayment payment) {

        return new SubscriptionPaymentResource(
                payment.invoiceId(),
                payment.invoiceNumber(),
                payment.amountPaid(),
                payment.currency(),
                "PAID",
                payment.paidAt(),
                payment.invoiceUrl(),
                payment.invoicePdfUrl()
        );
    }
}
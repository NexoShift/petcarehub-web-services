package com.nexoshift.petcarehub.subscriptions.domain.services;

import com.nexoshift.petcarehub.subscriptions.domain.model.queries
        .GetSubscriptionPaymentsByAccountIdQuery;
import com.nexoshift.petcarehub.subscriptions.domain.model.valueobjects
        .SubscriptionPaymentPage;

public interface SubscriptionPaymentQueryService {

    SubscriptionPaymentPage handle(
            GetSubscriptionPaymentsByAccountIdQuery query
    );
}
package com.nexoshift.petcarehub.subscriptions.domain.services;

import com.nexoshift.petcarehub.subscriptions.domain.model
        .aggregates.Subscription;
import com.nexoshift.petcarehub.subscriptions.domain.model
        .queries.GetSubscriptionEntitlementsByAccountIdQuery;

public interface SubscriptionEntitlementService {

    Subscription handle(
            GetSubscriptionEntitlementsByAccountIdQuery query
    );

    void ensureActiveSubscription(Long accountId);

    void lockActiveSubscriptionForRegistration(Long accountId);

    void ensurePetRegistrationAllowed(
            Long accountId,
            long registeredPets
    );

    void ensureVeterinarianRegistrationAllowed(
            Long accountId,
            long registeredVeterinarians
    );
}
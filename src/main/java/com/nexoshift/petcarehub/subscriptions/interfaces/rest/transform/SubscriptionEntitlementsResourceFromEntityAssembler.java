package com.nexoshift.petcarehub.subscriptions.interfaces.rest.transform;

import com.nexoshift.petcarehub.subscriptions.domain.model
        .aggregates.Subscription;
import com.nexoshift.petcarehub.subscriptions.interfaces.rest
        .resources.SubscriptionEntitlementsResource;

public class SubscriptionEntitlementsResourceFromEntityAssembler {

    public static SubscriptionEntitlementsResource toResourceFromEntity(
            Subscription entity) {

        var plan = entity.getPlan();
        var limits = plan.getLimits();

        return new SubscriptionEntitlementsResource(
                entity.getId(),
                entity.getAccountId(),
                plan.getCode().toString(),
                limits.getMaxPets(),
                limits.getMaxVeterinarians(),
                limits.getMonthlyComputerVisionCredits(),
                limits.getMonthlyLlmCredits()
        );
    }
}
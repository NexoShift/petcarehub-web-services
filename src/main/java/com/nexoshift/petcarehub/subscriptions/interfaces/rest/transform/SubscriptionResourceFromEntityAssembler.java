package com.nexoshift.petcarehub.subscriptions.interfaces.rest.transform;

import com.nexoshift.petcarehub.subscriptions.domain.model.aggregates.Subscription;
import com.nexoshift.petcarehub.subscriptions.interfaces.rest.resources.SubscriptionResource;

/**
 * Assembler for creating subscription resources from aggregates.
 */
public class SubscriptionResourceFromEntityAssembler {

    public static SubscriptionResource toResourceFromEntity(
            Subscription entity) {

        return new SubscriptionResource(
                entity.getId(),
                entity.getAccountId(),
                entity.getPlan().getId(),
                entity.getPlan().getCode(),
                entity.getStatus(),
                entity.getCurrentPeriodStart(),
                entity.getCurrentPeriodEnd(),
                entity.isCancelAtPeriodEnd()
        );
    }
}
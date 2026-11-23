package com.nexoshift.petcarehub.subscriptions.interfaces.rest.transform;

import com.nexoshift.petcarehub.subscriptions.domain.model.aggregates.Subscription;
import com.nexoshift.petcarehub.subscriptions.interfaces.rest.resources.CheckoutSessionResource;

public class CheckoutSessionResourceFromEntityAssembler {

    public static CheckoutSessionResource toResourceFromEntity(
            Subscription entity) {

        return new CheckoutSessionResource(
                entity.getId(),
                entity.getStripeCheckoutSessionId(),
                entity.getStripeCheckoutUrl(),
                entity.getCheckoutExpiresAt()
        );
    }
}
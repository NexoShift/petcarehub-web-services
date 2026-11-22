package com.nexoshift.petcarehub.subscriptions.interfaces.rest.resources;

import jakarta.validation.constraints.NotNull;

public record UpdateSubscriptionCancellationResource(

        @NotNull
        Boolean cancelAtPeriodEnd

) {
}
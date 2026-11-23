package com.nexoshift.petcarehub.subscriptions.domain.model.queries;

/**
 * Query to retrieve a subscription plan by its identifier.
 *
 * @param planId the subscription plan identifier
 */
public record GetPlanByIdQuery(Long planId) {

    public GetPlanByIdQuery {
        if (planId == null || planId <= 0) {
            throw new IllegalArgumentException(
                    "Plan ID must be a positive number");
        }
    }
}
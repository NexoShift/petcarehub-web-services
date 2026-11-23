package com.nexoshift.petcarehub.subscriptions.domain.services;

import com.nexoshift.petcarehub.subscriptions.domain.model.aggregates.Plan;
import com.nexoshift.petcarehub.subscriptions.domain.model.queries.GetPlanByIdQuery;
import com.nexoshift.petcarehub.subscriptions.domain.model.queries.GetPlansQuery;

import java.util.List;
import java.util.Optional;

/**
 * Service interface for querying subscription plans.
 */
public interface PlanQueryService {

    /**
     * Retrieves active subscription plans.
     *
     * @param query the plan search query
     * @return the matching plans
     */
    List<Plan> handle(GetPlansQuery query);

    /**
     * Retrieves an active subscription plan by its identifier.
     *
     * @param query the plan identifier query
     * @return the plan, if found
     */
    Optional<Plan> handle(GetPlanByIdQuery query);
}
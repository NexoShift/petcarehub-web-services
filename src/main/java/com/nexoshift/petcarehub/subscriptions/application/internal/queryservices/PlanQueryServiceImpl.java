package com.nexoshift.petcarehub.subscriptions.application.internal.queryservices;

import com.nexoshift.petcarehub.subscriptions.domain.model.aggregates.Plan;
import com.nexoshift.petcarehub.subscriptions.domain.model.queries.GetPlanByIdQuery;
import com.nexoshift.petcarehub.subscriptions.domain.model.queries.GetPlansQuery;
import com.nexoshift.petcarehub.subscriptions.domain.services.PlanQueryService;
import com.nexoshift.petcarehub.subscriptions.infrastructure.persistence.jpa.repositories.PlanRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Implementation of the subscription plan query service.
 *
 * @see PlanQueryService
 * @see PlanRepository
 */
@Service
@Transactional(readOnly = true)
public class PlanQueryServiceImpl implements PlanQueryService {

    private final PlanRepository planRepository;

    /**
     * Constructs the plan query service.
     *
     * @param planRepository the plan repository
     */
    public PlanQueryServiceImpl(PlanRepository planRepository) {
        this.planRepository = planRepository;
    }

    /**
     * Retrieves active subscription plans,
     * optionally filtered by subscriber type.
     *
     * @param query the plan search query
     * @return the matching plans
     */
    @Override
    public List<Plan> handle(GetPlansQuery query) {

        if (query.subscriberType() == null) {
            return planRepository.findAllByActiveTrueOrderByIdAsc();
        }

        return planRepository
                .findAllBySubscriberTypeAndActiveTrueOrderByIdAsc(
                        query.subscriberType()
                );
    }

    /**
     * Retrieves an active subscription plan by its identifier.
     *
     * @param query the plan identifier query
     * @return the plan, if found
     */
    @Override
    public Optional<Plan> handle(GetPlanByIdQuery query) {

        return planRepository.findById(query.planId())
                .filter(Plan::isActive);
    }
}
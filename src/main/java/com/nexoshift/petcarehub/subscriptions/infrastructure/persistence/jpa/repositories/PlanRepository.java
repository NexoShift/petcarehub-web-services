package com.nexoshift.petcarehub.subscriptions.infrastructure.persistence.jpa.repositories;

import com.nexoshift.petcarehub.subscriptions.domain.model.aggregates.Plan;
import com.nexoshift.petcarehub.subscriptions.domain.model.valueobjects.PlanCode;
import com.nexoshift.petcarehub.subscriptions.domain.model.valueobjects.SubscriberType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for managing subscription plans.
 */
@Repository
public interface PlanRepository extends JpaRepository<Plan, Long> {

    /**
     * Checks whether a plan exists with the specified code.
     *
     * @param code the plan code
     * @return true if the plan exists
     */
    boolean existsByCode(PlanCode code);

    /**
     * Retrieves all active subscription plans.
     *
     * @return the active plans
     */
    List<Plan> findAllByActiveTrueOrderByIdAsc();

    /**
     * Retrieves active plans for a subscriber type.
     *
     * @param subscriberType the subscriber type
     * @return the matching plans
     */
    List<Plan> findAllBySubscriberTypeAndActiveTrueOrderByIdAsc(
            SubscriberType subscriberType);
}
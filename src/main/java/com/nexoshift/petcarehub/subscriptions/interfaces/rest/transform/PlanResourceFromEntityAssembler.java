package com.nexoshift.petcarehub.subscriptions.interfaces.rest.transform;

import com.nexoshift.petcarehub.subscriptions.domain.model.aggregates.Plan;
import com.nexoshift.petcarehub.subscriptions.interfaces.rest.resources.PlanResource;

/**
 * Assembler for creating a plan resource from an aggregate.
 */
public class PlanResourceFromEntityAssembler {

    /**
     * Converts a plan aggregate into a resource.
     *
     * @param entity the plan aggregate
     * @return the plan resource
     */
    public static PlanResource toResourceFromEntity(Plan entity) {

        var limits = entity.getLimits();

        return new PlanResource(
                entity.getId(),
                entity.getCode(),
                entity.getName(),
                entity.getSubscriberType(),
                entity.getPlanType(),
                entity.getMonthlyPrice(),
                entity.getCurrency(),
                limits.getMaxPets(),
                limits.getMaxVeterinarians(),
                limits.getMonthlyComputerVisionCredits(),
                limits.getMonthlyLlmCredits()
        );
    }
}
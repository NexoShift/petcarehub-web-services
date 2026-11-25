package com.nexoshift.petcarehub.subscriptions.interfaces.rest.resources;

import com.nexoshift.petcarehub.subscriptions.domain.model.valueobjects.PlanCode;
import com.nexoshift.petcarehub.subscriptions.domain.model.valueobjects.PlanType;
import com.nexoshift.petcarehub.subscriptions.domain.model.valueobjects.SubscriberType;

import java.math.BigDecimal;

/**
 * Resource representing a subscription plan.
 *
 * @param id the plan identifier
 * @param code the unique plan code
 * @param name the plan name
 * @param subscriberType the target account segment
 * @param planType the plan tier
 * @param monthlyPrice the monthly subscription price
 * @param currency the billing currency
 * @param maxPets the maximum number of pets
 * @param maxVeterinarians the maximum number of veterinarians
 * @param monthlyComputerVisionCredits the monthly CV credits
 * @param monthlyLlmCredits the monthly LLM credits
 */
public record PlanResource(
        Long id,
        PlanCode code,
        String name,
        SubscriberType subscriberType,
        PlanType planType,
        BigDecimal monthlyPrice,
        String currency,
        Integer maxPets,
        Integer maxVeterinarians,
        Integer monthlyComputerVisionCredits,
        Integer monthlyLlmCredits
) {
}
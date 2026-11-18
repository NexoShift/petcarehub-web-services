package com.nexoshift.petcarehub.subscriptions.domain.model.valueobjects;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

/**
 * Defines the resource and AI usage limits of a subscription plan.
 */
@Embeddable
public class PlanLimits {

    @Column(name = "max_pets")
    private Integer maxPets;

    @Column(name = "max_veterinarians")
    private Integer maxVeterinarians;

    @Column(name = "monthly_cv_credits", nullable = false)
    private Integer monthlyComputerVisionCredits;

    @Column(name = "monthly_llm_credits", nullable = false)
    private Integer monthlyLlmCredits;

    protected PlanLimits() {
    }

    public PlanLimits(
            Integer maxPets,
            Integer maxVeterinarians,
            Integer monthlyComputerVisionCredits,
            Integer monthlyLlmCredits) {

        if (maxPets != null && maxPets <= 0) {
            throw new IllegalArgumentException(
                    "Maximum pets must be positive");
        }

        if (maxVeterinarians != null && maxVeterinarians <= 0) {
            throw new IllegalArgumentException(
                    "Maximum veterinarians must be positive");
        }

        if ((maxPets == null) == (maxVeterinarians == null)) {
            throw new IllegalArgumentException(
                    "Exactly one account resource limit is required");
        }

        if (monthlyComputerVisionCredits == null
                || monthlyComputerVisionCredits < 0) {
            throw new IllegalArgumentException(
                    "Computer vision credits cannot be negative");
        }

        if (monthlyLlmCredits == null || monthlyLlmCredits < 0) {
            throw new IllegalArgumentException(
                    "LLM credits cannot be negative");
        }

        this.maxPets = maxPets;
        this.maxVeterinarians = maxVeterinarians;
        this.monthlyComputerVisionCredits = monthlyComputerVisionCredits;
        this.monthlyLlmCredits = monthlyLlmCredits;
    }

    public Integer getMaxPets() {
        return maxPets;
    }

    public Integer getMaxVeterinarians() {
        return maxVeterinarians;
    }

    public Integer getMonthlyComputerVisionCredits() {
        return monthlyComputerVisionCredits;
    }

    public Integer getMonthlyLlmCredits() {
        return monthlyLlmCredits;
    }
}
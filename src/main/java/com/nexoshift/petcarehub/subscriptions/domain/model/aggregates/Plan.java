package com.nexoshift.petcarehub.subscriptions.domain.model.aggregates;

import com.nexoshift.petcarehub.shared.domain.model.aggregates.AuditableAbstractAggregateRoot;
import com.nexoshift.petcarehub.subscriptions.domain.model.valueobjects.PlanCode;
import com.nexoshift.petcarehub.subscriptions.domain.model.valueobjects.PlanLimits;
import com.nexoshift.petcarehub.subscriptions.domain.model.valueobjects.PlanType;
import com.nexoshift.petcarehub.subscriptions.domain.model.valueobjects.SubscriberType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Getter;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Aggregate root representing a subscription plan.
 */
@Getter
@Entity
public class Plan extends AuditableAbstractAggregateRoot<Plan> {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, unique = true, updatable = false, length = 40)
    private PlanCode code;

    @Column(nullable = false, length = 100)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SubscriberType subscriberType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PlanType planType;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal monthlyPrice;

    @Column(nullable = false, length = 3)
    private String currency;

    @Embedded
    private PlanLimits limits;

    @Column(nullable = false)
    private boolean active;

    protected Plan() {
        super();
    }

    /**
     * Creates a subscription plan with its price and resource limits.
     */
    public Plan(
            PlanCode code,
            String name,
            BigDecimal monthlyPrice,
            PlanLimits limits) {

        this();

        if (code == null) {
            throw new IllegalArgumentException(
                    "Plan code is required");
        }

        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException(
                    "Plan name is required");
        }

        if (monthlyPrice == null || monthlyPrice.signum() <= 0) {
            throw new IllegalArgumentException(
                    "Monthly price must be positive");
        }

        if (limits == null) {
            throw new IllegalArgumentException(
                    "Plan limits are required");
        }

        if (code.subscriberType() == SubscriberType.PET_OWNER
                && limits.getMaxPets() == null) {
            throw new IllegalArgumentException(
                    "Pet owner plans require a pet limit");
        }

        if (code.subscriberType() == SubscriberType.CLINIC
                && limits.getMaxVeterinarians() == null) {
            throw new IllegalArgumentException(
                    "Clinic plans require a veterinarian limit");
        }

        this.code = code;
        this.name = name.trim();
        this.subscriberType = code.subscriberType();
        this.planType = code.planType();
        this.monthlyPrice = monthlyPrice.setScale(
                2, RoundingMode.UNNECESSARY);
        this.currency = "PEN";
        this.limits = limits;
        this.active = true;
    }

    /**
     * Disables the plan for new subscriptions.
     */
    public void deactivate() {
        this.active = false;
    }

    /**
     * Enables the plan for new subscriptions.
     */
    public void activate() {
        this.active = true;
    }
}
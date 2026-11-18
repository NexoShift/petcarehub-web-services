package com.nexoshift.petcarehub.subscriptions.domain.model.valueobjects;

/**
 * Identifies each available subscription plan.
 */
public enum PlanCode {

    PET_OWNER_BASIC(
            SubscriberType.PET_OWNER,
            PlanType.BASIC
    ),

    PET_OWNER_PREMIUM(
            SubscriberType.PET_OWNER,
            PlanType.PREMIUM
    ),

    CLINIC_BASIC(
            SubscriberType.CLINIC,
            PlanType.BASIC
    ),

    CLINIC_PREMIUM(
            SubscriberType.CLINIC,
            PlanType.PREMIUM
    );

    private final SubscriberType subscriberType;
    private final PlanType planType;

    PlanCode(SubscriberType subscriberType, PlanType planType) {
        this.subscriberType = subscriberType;
        this.planType = planType;
    }

    public SubscriberType subscriberType() {
        return subscriberType;
    }

    public PlanType planType() {
        return planType;
    }
}
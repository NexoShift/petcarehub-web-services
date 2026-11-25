package com.nexoshift.petcarehub.subscriptions.domain.model.valueobjects;

/**
 * Represents the lifecycle status of a subscription.
 */
public enum SubscriptionStatus {
    PENDING,
    ACTIVE,
    PAST_DUE,
    CANCELED,
    EXPIRED
}
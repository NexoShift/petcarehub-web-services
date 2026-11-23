package com.nexoshift.petcarehub.subscriptions.domain.model.queries;

import com.nexoshift.petcarehub.subscriptions.domain.model.valueobjects.SubscriberType;

/**
 * Query to retrieve active subscription plans,
 * optionally filtered by subscriber type.
 *
 * @param subscriberType the account segment, or null for all plans
 */
public record GetPlansQuery(SubscriberType subscriberType) {
}
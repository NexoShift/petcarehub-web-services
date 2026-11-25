package com.nexoshift.petcarehub.subscriptions.domain.services;

import com.nexoshift.petcarehub.subscriptions.domain.model.aggregates.Subscription;
import com.nexoshift.petcarehub.subscriptions.domain.model.queries.GetSubscriptionByAccountIdQuery;

import java.util.Optional;

/**
 * Service for retrieving subscriptions.
 */
public interface SubscriptionQueryService {

    Optional<Subscription> handle(GetSubscriptionByAccountIdQuery query);
}
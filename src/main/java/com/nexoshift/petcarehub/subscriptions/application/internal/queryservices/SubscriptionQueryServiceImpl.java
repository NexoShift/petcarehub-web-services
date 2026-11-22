package com.nexoshift.petcarehub.subscriptions.application.internal.queryservices;

import com.nexoshift.petcarehub.subscriptions.application.internal.outboundservices.acl.ExternalIamService;
import com.nexoshift.petcarehub.subscriptions.domain.exceptions.SubscriptionAccessDeniedException;
import com.nexoshift.petcarehub.subscriptions.domain.model.aggregates.Subscription;
import com.nexoshift.petcarehub.subscriptions.domain.model.queries.GetSubscriptionByAccountIdQuery;
import com.nexoshift.petcarehub.subscriptions.domain.services.SubscriptionQueryService;
import com.nexoshift.petcarehub.subscriptions.infrastructure.persistence.jpa.repositories.SubscriptionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Implementation of the subscription query service.
 */
@Service
@Transactional(readOnly = true)
public class SubscriptionQueryServiceImpl
        implements SubscriptionQueryService {

    private final SubscriptionRepository subscriptionRepository;
    private final ExternalIamService externalIamService;

    public SubscriptionQueryServiceImpl(
            SubscriptionRepository subscriptionRepository,
            ExternalIamService externalIamService) {

        this.subscriptionRepository = subscriptionRepository;
        this.externalIamService = externalIamService;
    }

    @Override
    public Optional<Subscription> handle(
            GetSubscriptionByAccountIdQuery query) {

        var authenticatedUserId =
                externalIamService.fetchAuthenticatedUserId();

        if (!externalIamService.existsActiveAccount(query.accountId())
                || !externalIamService.isAccountHolder(
                authenticatedUserId,
                query.accountId())) {

            throw new SubscriptionAccessDeniedException();
        }

        return subscriptionRepository.findByAccountId(query.accountId());
    }
}
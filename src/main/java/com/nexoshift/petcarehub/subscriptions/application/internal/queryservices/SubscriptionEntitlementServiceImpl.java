package com.nexoshift.petcarehub.subscriptions.application.internal.queryservices;

import com.nexoshift.petcarehub.shared.domain.exceptions.ForbiddenException;
import com.nexoshift.petcarehub.subscriptions.application.internal
        .outboundservices.acl.ExternalIamService;
import com.nexoshift.petcarehub.subscriptions.domain.exceptions
        .ActiveSubscriptionRequiredException;
import com.nexoshift.petcarehub.subscriptions.domain.model
        .aggregates.Subscription;
import com.nexoshift.petcarehub.subscriptions.domain.model
        .queries.GetSubscriptionEntitlementsByAccountIdQuery;
import com.nexoshift.petcarehub.subscriptions.domain.services
        .SubscriptionEntitlementService;
import com.nexoshift.petcarehub.subscriptions.infrastructure.persistence
        .jpa.repositories.SubscriptionRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
public class SubscriptionEntitlementServiceImpl
        implements SubscriptionEntitlementService {

    private final SubscriptionRepository subscriptionRepository;
    private final ExternalIamService externalIamService;

    public SubscriptionEntitlementServiceImpl(
            SubscriptionRepository subscriptionRepository,
            ExternalIamService externalIamService) {

        this.subscriptionRepository = subscriptionRepository;
        this.externalIamService = externalIamService;
    }

    @Override
    @Transactional(readOnly = true)
    public Subscription handle(
            GetSubscriptionEntitlementsByAccountIdQuery query) {

        ensureAccountAccess(query.accountId());

        return findActiveSubscription(query.accountId());
    }

    @Override
    @Transactional(readOnly = true)
    public void ensureActiveSubscription(Long accountId) {
        findActiveSubscription(accountId);
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public void lockActiveSubscriptionForRegistration(Long accountId) {
        findActiveSubscriptionForUpdate(accountId);
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public void ensurePetRegistrationAllowed(
            Long accountId,
            long registeredPets) {

        var subscription = findActiveSubscriptionForUpdate(accountId);

        subscription.getPlan()
                .getLimits()
                .ensurePetRegistrationAllowed(registeredPets);
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public void ensureVeterinarianRegistrationAllowed(
            Long accountId,
            long registeredVeterinarians) {

        var subscription = findActiveSubscriptionForUpdate(accountId);

        subscription.getPlan()
                .getLimits()
                .ensureVeterinarianRegistrationAllowed(
                        registeredVeterinarians
                );
    }

    private Subscription findActiveSubscription(Long accountId) {

        ensureAccountIsActive(accountId);

        var subscription = subscriptionRepository
                .findByAccountId(accountId)
                .orElseThrow(ActiveSubscriptionRequiredException::new);

        validateSubscription(subscription);

        return subscription;
    }

    private Subscription findActiveSubscriptionForUpdate(Long accountId) {

        ensureAccountIsActive(accountId);

        var subscription = subscriptionRepository
                .findByAccountIdForUpdate(accountId)
                .orElseThrow(ActiveSubscriptionRequiredException::new);

        validateSubscription(subscription);

        return subscription;
    }

    private void validateSubscription(Subscription subscription) {
        if (!subscription.isActive()) {
            throw new ActiveSubscriptionRequiredException();
        }
    }

    private void ensureAccountIsActive(Long accountId) {

        if (accountId == null || accountId <= 0) {
            throw new IllegalArgumentException(
                    "Account ID must be a positive number."
            );
        }

        if (!externalIamService.existsActiveAccount(accountId)) {
            throw new ForbiddenException(
                    "The account does not exist or is not active."
            );
        }
    }

    private void ensureAccountAccess(Long accountId) {

        Long authenticatedUserId =
                externalIamService.fetchAuthenticatedUserId();

        if (authenticatedUserId == null || authenticatedUserId <= 0) {
            throw new ForbiddenException(
                    "An authenticated user is required."
            );
        }

        Long authenticatedAccountId =
                externalIamService.fetchAccountIdByUserId(
                        authenticatedUserId
                );

        if (!Objects.equals(accountId, authenticatedAccountId)) {
            throw new ForbiddenException(
                    "You do not have access to this account."
            );
        }
    }
}
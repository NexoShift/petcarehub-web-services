package com.nexoshift.petcarehub.subscriptions.application.internal.commandservices;

import com.nexoshift.petcarehub.subscriptions.application.internal.outboundservices.acl.ExternalIamService;
import com.nexoshift.petcarehub.subscriptions.domain.exceptions.SubscriptionAccessDeniedException;
import com.nexoshift.petcarehub.subscriptions.domain.exceptions.SubscriptionNotFoundException;
import com.nexoshift.petcarehub.subscriptions.domain.model.aggregates.Subscription;
import com.nexoshift.petcarehub.subscriptions.domain.model.commands.SimulateSubscriptionPaymentCommand;
import com.nexoshift.petcarehub.subscriptions.domain.model.valueobjects.SubscriptionStatus;
import com.nexoshift.petcarehub.subscriptions.domain.services.SimulatedPaymentCommandService;
import com.nexoshift.petcarehub.subscriptions.infrastructure.persistence.jpa.repositories.SubscriptionRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

/**
 * Does what the Stripe webhook does after a successful payment: synchronizes the
 * subscription as ACTIVE for 30 days. The aggregate then publishes
 * SubscriptionPeriodPaidEvent, which opens the AI credit period, exactly as in production.
 * <p>
 * The bean only exists with subscriptions.simulated-payments.enabled=true.
 */
@Service
@ConditionalOnProperty(name = "subscriptions.simulated-payments.enabled", havingValue = "true")
public class SimulatedPaymentCommandServiceImpl implements SimulatedPaymentCommandService {

    private static final long PERIOD_DAYS = 30;

    private final SubscriptionRepository subscriptionRepository;
    private final ExternalIamService externalIamService;

    public SimulatedPaymentCommandServiceImpl(
            SubscriptionRepository subscriptionRepository,
            ExternalIamService externalIamService) {
        this.subscriptionRepository = subscriptionRepository;
        this.externalIamService = externalIamService;
    }

    @Override
    @Transactional
    public Subscription handle(SimulateSubscriptionPaymentCommand command) {

        var accountId = command.accountId();
        var authenticatedUserId = externalIamService.fetchAuthenticatedUserId();

        // Only the account holder pays for the account, as in the real checkout.
        if (authenticatedUserId == null
                || !externalIamService.isAccountHolder(authenticatedUserId, accountId)
                || !Objects.equals(
                        externalIamService.fetchAccountIdByUserId(authenticatedUserId), accountId)) {
            throw new SubscriptionAccessDeniedException();
        }

        var subscription = subscriptionRepository.findByAccountIdForUpdate(accountId)
                .orElseThrow(() -> new SubscriptionNotFoundException(accountId));

        // Keep the provider ids if the subscription already has them (they cannot change).
        var customerId = Objects.requireNonNullElse(
                subscription.getStripeCustomerId(), "cus_simulated_" + accountId);
        var providerSubscriptionId = Objects.requireNonNullElse(
                subscription.getStripeSubscriptionId(), "sub_simulated_" + accountId);

        var periodStart = Instant.now();
        subscription.synchronizeBilling(
                customerId,
                providerSubscriptionId,
                SubscriptionStatus.ACTIVE,
                periodStart,
                periodStart.plus(PERIOD_DAYS, ChronoUnit.DAYS),
                false);

        return subscriptionRepository.save(subscription);
    }
}

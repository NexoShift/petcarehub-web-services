package com.nexoshift.petcarehub.subscriptions.application.internal.commandservices;

import com.nexoshift.petcarehub.shared.domain.exceptions.ForbiddenException;
import com.nexoshift.petcarehub.subscriptions.application.internal
        .outboundservices.acl.ExternalIamService;
import com.nexoshift.petcarehub.subscriptions.application.internal
        .outboundservices.payments.PaymentGateway;
import com.nexoshift.petcarehub.subscriptions.domain.exceptions
        .SubscriptionCancellationNotAllowedException;
import com.nexoshift.petcarehub.subscriptions.domain.model.commands
        .UpdateSubscriptionCancellationCommand;
import com.nexoshift.petcarehub.subscriptions.domain.services
        .SubscriptionCancellationCommandService;
import com.nexoshift.petcarehub.subscriptions.infrastructure.persistence
        .jpa.repositories.SubscriptionRepository;

import org.springframework.stereotype.Service;

@Service
public class SubscriptionCancellationCommandServiceImpl
        implements SubscriptionCancellationCommandService {

    private final SubscriptionRepository subscriptionRepository;
    private final ExternalIamService externalIamService;
    private final PaymentGateway paymentGateway;

    public SubscriptionCancellationCommandServiceImpl(
            SubscriptionRepository subscriptionRepository,
            ExternalIamService externalIamService,
            PaymentGateway paymentGateway) {

        this.subscriptionRepository = subscriptionRepository;
        this.externalIamService = externalIamService;
        this.paymentGateway = paymentGateway;
    }

    @Override
    public void handle(
            UpdateSubscriptionCancellationCommand command) {

        ensureAccountHolder(command.accountId());

        var subscription = subscriptionRepository
                .findByAccountId(command.accountId())
                .orElseThrow(() ->
                        new SubscriptionCancellationNotAllowedException(
                                "The account does not have a subscription."
                        )
                );

        String stripeSubscriptionId =
                subscription.getStripeSubscriptionId();

        if (stripeSubscriptionId == null
                || stripeSubscriptionId.isBlank()) {

            throw new SubscriptionCancellationNotAllowedException(
                    "The subscription has not been activated in Stripe."
            );
        }

        paymentGateway.updateSubscriptionCancellation(
                stripeSubscriptionId,
                command.cancelAtPeriodEnd()
        );
    }

    private void ensureAccountHolder(Long accountId) {

        Long authenticatedUserId =
                externalIamService.fetchAuthenticatedUserId();

        if (!externalIamService.existsActiveAccount(accountId)) {

            throw new ForbiddenException(
                    "The account does not exist or is not active."
            );
        }

        if (authenticatedUserId == null
                || authenticatedUserId <= 0
                || !externalIamService.isAccountHolder(
                authenticatedUserId,
                accountId
        )) {

            throw new ForbiddenException(
                    "Only the account holder can update "
                            + "the subscription cancellation."
            );
        }
    }
}
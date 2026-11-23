package com.nexoshift.petcarehub.subscriptions.application.internal.queryservices;

import com.nexoshift.petcarehub.shared.domain.exceptions.ForbiddenException;
import com.nexoshift.petcarehub.subscriptions.application.internal
        .outboundservices.acl.ExternalIamService;
import com.nexoshift.petcarehub.subscriptions.application.internal
        .outboundservices.payments.PaymentGateway;
import com.nexoshift.petcarehub.subscriptions.domain.model.queries
        .GetSubscriptionPaymentsByAccountIdQuery;
import com.nexoshift.petcarehub.subscriptions.domain.model.valueobjects
        .SubscriptionPaymentPage;
import com.nexoshift.petcarehub.subscriptions.domain.services
        .SubscriptionPaymentQueryService;
import com.nexoshift.petcarehub.subscriptions.infrastructure.persistence
        .jpa.repositories.SubscriptionRepository;

import org.springframework.stereotype.Service;

@Service
public class SubscriptionPaymentQueryServiceImpl
        implements SubscriptionPaymentQueryService {

    private final SubscriptionRepository subscriptionRepository;
    private final ExternalIamService externalIamService;
    private final PaymentGateway paymentGateway;

    public SubscriptionPaymentQueryServiceImpl(
            SubscriptionRepository subscriptionRepository,
            ExternalIamService externalIamService,
            PaymentGateway paymentGateway) {

        this.subscriptionRepository = subscriptionRepository;
        this.externalIamService = externalIamService;
        this.paymentGateway = paymentGateway;
    }

    @Override
    public SubscriptionPaymentPage handle(
            GetSubscriptionPaymentsByAccountIdQuery query) {

        ensureAccountHolder(query.accountId());

        var subscription = subscriptionRepository
                .findByAccountId(query.accountId());

        if (subscription.isEmpty()) {
            return SubscriptionPaymentPage.empty();
        }

        String stripeSubscriptionId =
                subscription.get().getStripeSubscriptionId();

        if (stripeSubscriptionId == null
                || stripeSubscriptionId.isBlank()) {

            return SubscriptionPaymentPage.empty();
        }

        return paymentGateway.retrieveSubscriptionPayments(
                stripeSubscriptionId,
                query.limit(),
                query.startingAfter()
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
                    "Only the account holder can access "
                            + "the payment history."
            );
        }
    }
}
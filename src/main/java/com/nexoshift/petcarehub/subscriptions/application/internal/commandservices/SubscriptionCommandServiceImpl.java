package com.nexoshift.petcarehub.subscriptions.application.internal.commandservices;

import com.nexoshift.petcarehub.subscriptions.application.internal.outboundservices.acl.ExternalIamService;
import com.nexoshift.petcarehub.subscriptions.application.internal.outboundservices.payments.CheckoutSessionData;
import com.nexoshift.petcarehub.subscriptions.application.internal.outboundservices.payments.PaymentGateway;
import com.nexoshift.petcarehub.subscriptions.domain.exceptions.CheckoutAlreadyCompletedException;
import com.nexoshift.petcarehub.subscriptions.domain.exceptions.CheckoutUnavailableException;
import com.nexoshift.petcarehub.subscriptions.domain.exceptions.IncompatiblePlanException;
import com.nexoshift.petcarehub.subscriptions.domain.exceptions.InvalidSubscriptionStatusException;
import com.nexoshift.petcarehub.subscriptions.domain.exceptions.PlanNotAvailableException;
import com.nexoshift.petcarehub.subscriptions.domain.exceptions.PlanNotFoundException;
import com.nexoshift.petcarehub.subscriptions.domain.exceptions.SubscriptionAccessDeniedException;
import com.nexoshift.petcarehub.subscriptions.domain.exceptions.SubscriptionAlreadyExistsException;
import com.nexoshift.petcarehub.subscriptions.domain.exceptions.SubscriptionNotFoundException;
import com.nexoshift.petcarehub.subscriptions.domain.model.aggregates.Subscription;
import com.nexoshift.petcarehub.subscriptions.domain.model.commands.CreateCheckoutSessionCommand;
import com.nexoshift.petcarehub.subscriptions.domain.model.commands.CreateSubscriptionCommand;
import com.nexoshift.petcarehub.subscriptions.domain.model.valueobjects.SubscriptionStatus;
import com.nexoshift.petcarehub.subscriptions.domain.services.SubscriptionCommandService;
import com.nexoshift.petcarehub.subscriptions.infrastructure.persistence.jpa.repositories.PlanRepository;
import com.nexoshift.petcarehub.subscriptions.infrastructure.persistence.jpa.repositories.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

@Service
@RequiredArgsConstructor
public class SubscriptionCommandServiceImpl
        implements SubscriptionCommandService {

    private final SubscriptionRepository subscriptionRepository;
    private final PlanRepository planRepository;
    private final ExternalIamService externalIamService;
    private final PaymentGateway paymentGateway;
    private final PlatformTransactionManager transactionManager;

    @Override
    @Transactional
    public Long handle(CreateSubscriptionCommand command) {

        validateAccountAccess(command.accountId());

        var plan = planRepository.findById(command.planId())
                .orElseThrow(() ->
                        new PlanNotFoundException(command.planId()));

        if (!plan.isActive()) {
            throw new PlanNotAvailableException(plan.getId());
        }

        String accountType = externalIamService
                .fetchAccountTypeByAccountId(command.accountId());

        if (!plan.getSubscriberType().name().equals(accountType)) {
            throw new IncompatiblePlanException();
        }

        if (subscriptionRepository.existsByAccountId(
                command.accountId())) {

            throw new SubscriptionAlreadyExistsException(
                    command.accountId());
        }

        var subscription = new Subscription(command, plan);

        return subscriptionRepository.save(subscription).getId();
    }

    @Override
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public Subscription handle(CreateCheckoutSessionCommand command) {

        validateAccountAccess(command.accountId());

        var transactionTemplate =
                new TransactionTemplate(transactionManager);

        CheckoutResult result = transactionTemplate.execute(
                status -> prepareCheckoutAttempt(command.accountId())
        );

        if (result == null) {

            result = transactionTemplate.execute(
                    status -> createOrRetrieveCheckoutSession(
                            command.accountId())
            );
        }

        if (result == null) {
            throw new CheckoutUnavailableException();
        }

        if (result.data().isCompleted()) {
            throw new CheckoutAlreadyCompletedException();
        }

        if (!result.data().isOpen()
                || !result.subscription().hasAvailableCheckout()) {

            throw new CheckoutUnavailableException();
        }

        return result.subscription();
    }

    private CheckoutResult prepareCheckoutAttempt(Long accountId) {

        var subscription = findPendingSubscriptionForUpdate(accountId);

        if (subscription.getStripeCheckoutSessionId() != null) {

            var session = paymentGateway.retrieveCheckoutSession(
                    subscription.getStripeCheckoutSessionId());

            if (!session.isExpired()) {
                return synchronizeCheckoutSession(
                        subscription, session);
            }

            validatePlanAvailability(subscription);

            subscription.startCheckoutAttempt();

            return null;
        }

        validatePlanAvailability(subscription);

        if (!subscription.hasCheckoutAttempt()) {
            subscription.startCheckoutAttempt();
        }

        return null;
    }

    private CheckoutResult createOrRetrieveCheckoutSession(
            Long accountId) {

        var subscription = findPendingSubscriptionForUpdate(accountId);

        if (subscription.getStripeCheckoutSessionId() != null) {

            var session = paymentGateway.retrieveCheckoutSession(
                    subscription.getStripeCheckoutSessionId());

            return synchronizeCheckoutSession(subscription, session);
        }

        if (!subscription.canRetryCheckoutAttempt()) {
            throw new CheckoutUnavailableException();
        }

        validatePlanAvailability(subscription);

        var session = paymentGateway.createCheckoutSession(
                subscription.getId(),
                subscription.getAccountId(),
                subscription.getPlan(),
                subscription.getCheckoutAttemptId()
        );

        var currentSession = paymentGateway.retrieveCheckoutSession(
                session.sessionId());

        return synchronizeCheckoutSession(
                subscription, currentSession);
    }

    private CheckoutResult synchronizeCheckoutSession(
            Subscription subscription,
            CheckoutSessionData session) {

        subscription.assignCheckoutSession(
                session.sessionId(),
                session.checkoutUrl(),
                session.expiresAt()
        );

        return new CheckoutResult(subscription, session);
    }

    private Subscription findPendingSubscriptionForUpdate(
            Long accountId) {

        var subscription = subscriptionRepository
                .findByAccountIdForUpdate(accountId)
                .orElseThrow(() ->
                        new SubscriptionNotFoundException(accountId));

        if (subscription.getStatus() != SubscriptionStatus.PENDING) {
            throw new InvalidSubscriptionStatusException();
        }

        if (subscription.getStripeSubscriptionId() != null) {
            throw new CheckoutAlreadyCompletedException();
        }

        return subscription;
    }

    private void validatePlanAvailability(Subscription subscription) {

        if (!subscription.getPlan().isActive()) {

            throw new PlanNotAvailableException(
                    subscription.getPlan().getId());
        }
    }

    private void validateAccountAccess(Long accountId) {

        Long requesterUserId =
                externalIamService.fetchAuthenticatedUserId();

        if (requesterUserId == null
                || requesterUserId <= 0
                || !externalIamService.existsActiveAccount(accountId)
                || !externalIamService.isAccountHolder(
                requesterUserId, accountId)) {

            throw new SubscriptionAccessDeniedException();
        }
    }

    private record CheckoutResult(
            Subscription subscription,
            CheckoutSessionData data
    ) {}
}
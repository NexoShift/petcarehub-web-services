package com.nexoshift.petcarehub.subscriptions.application.internal.commandservices;

import com.nexoshift.petcarehub.subscriptions.application.internal.outboundservices.payments.PaymentWebhookData;
import com.nexoshift.petcarehub.subscriptions.application.internal.outboundservices.payments.PaymentWebhookGateway;
import com.nexoshift.petcarehub.subscriptions.application.internal.outboundservices.payments.SubscriptionPaymentData;
import com.nexoshift.petcarehub.subscriptions.domain.exceptions.PaymentSynchronizationException;
import com.nexoshift.petcarehub.subscriptions.domain.model.aggregates.Subscription;
import com.nexoshift.petcarehub.subscriptions.domain.model.commands.ProcessPaymentWebhookCommand;
import com.nexoshift.petcarehub.subscriptions.domain.model.valueobjects.SubscriptionStatus;
import com.nexoshift.petcarehub.subscriptions.domain.services.PaymentWebhookCommandService;
import com.nexoshift.petcarehub.subscriptions.infrastructure.persistence.jpa.entities.PaymentWebhookReceipt;
import com.nexoshift.petcarehub.subscriptions.infrastructure.persistence.jpa.repositories.PaymentWebhookReceiptRepository;
import com.nexoshift.petcarehub.subscriptions.infrastructure.persistence.jpa.repositories.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentWebhookCommandServiceImpl
        implements PaymentWebhookCommandService {

    private final SubscriptionRepository subscriptionRepository;

    private final PaymentWebhookReceiptRepository
            paymentWebhookReceiptRepository;

    private final PaymentWebhookGateway paymentWebhookGateway;

    @Override
    @Transactional
    public void handle(ProcessPaymentWebhookCommand command) {

        var optionalEvent = paymentWebhookGateway.verifyAndResolve(
                command.payload(),
                command.signature()
        );

        if (optionalEvent.isEmpty()) {
            return;
        }

        var event = optionalEvent.get();

        var subscription = subscriptionRepository
                .findByAccountIdForUpdate(event.accountId())
                .orElseThrow(PaymentSynchronizationException::new);

        if (paymentWebhookReceiptRepository
                .existsById(event.eventId())) {

            log.debug(
                    "Payment webhook {} has already been processed",
                    event.eventId()
            );

            return;
        }

        validateEvent(subscription, event);

        if (event.isCheckoutExpired()) {

            handleCheckoutExpired(subscription, event);

        } else {

            synchronizeSubscription(subscription, event);
        }

        subscriptionRepository.save(subscription);

        paymentWebhookReceiptRepository.save(
                new PaymentWebhookReceipt(
                        event.eventId(),
                        event.eventType()
                )
        );

        log.info(
                "Processed payment event {} for subscription {}",
                event.eventType(),
                subscription.getId()
        );
    }

    private void synchronizeSubscription(
            Subscription subscription,
            PaymentWebhookData event) {

        var paymentData = paymentWebhookGateway
                .retrieveSubscriptionPaymentData(
                        event.stripeSubscriptionId(),
                        event.invoiceId()
                );

        validatePaymentData(subscription, paymentData);

        validateSubscriptionBinding(subscription, paymentData);

        if (event.isCheckoutCompleted()) {

            subscription.recordCompletedCheckout(
                    event.checkoutSessionId(),
                    event.checkoutExpiresAt()
            );
        }

        subscription.synchronizeBilling(
                paymentData.stripeCustomerId(),
                paymentData.stripeSubscriptionId(),
                paymentData.status(),
                paymentData.paidPeriodStart(),
                paymentData.paidPeriodEnd(),
                paymentData.cancelAtPeriodEnd()
        );
    }

    private void handleCheckoutExpired(
            Subscription subscription,
            PaymentWebhookData event) {

        if (subscription.getStatus() != SubscriptionStatus.PENDING
                || subscription.getStripeSubscriptionId() != null) {

            return;
        }

        String currentSessionId =
                subscription.getStripeCheckoutSessionId();

        boolean matchingSession = currentSessionId != null
                && currentSessionId.equals(
                event.checkoutSessionId());

        boolean matchingAttempt = currentSessionId == null
                && subscription.getCheckoutAttemptId() != null
                && subscription.getCheckoutAttemptId().equals(
                event.checkoutAttemptId());

        if (matchingSession || matchingAttempt) {

            subscription.assignCheckoutSession(
                    event.checkoutSessionId(),
                    null,
                    event.checkoutExpiresAt()
            );
        }
    }

    private void validateEvent(
            Subscription subscription,
            PaymentWebhookData event) {

        if (!Objects.equals(
                subscription.getId(),
                event.subscriptionId())

                || !Objects.equals(
                subscription.getAccountId(),
                event.accountId())

                || subscription.getPlan().getCode()
                != event.planCode()) {

            throw new PaymentSynchronizationException();
        }
    }

    private void validatePaymentData(
            Subscription subscription,
            SubscriptionPaymentData paymentData) {

        if (!Objects.equals(
                subscription.getId(),
                paymentData.subscriptionId())

                || !Objects.equals(
                subscription.getAccountId(),
                paymentData.accountId())

                || subscription.getPlan().getCode()
                != paymentData.planCode()) {

            throw new PaymentSynchronizationException();
        }
    }

    private void validateSubscriptionBinding(
            Subscription subscription,
            SubscriptionPaymentData paymentData) {

        String existingStripeSubscriptionId =
                subscription.getStripeSubscriptionId();

        if (existingStripeSubscriptionId != null) {

            if (!existingStripeSubscriptionId.equals(
                    paymentData.stripeSubscriptionId())) {

                throw new PaymentSynchronizationException();
            }

            return;
        }

        if (paymentData.checkoutAttemptId() != null) {

            if (!Objects.equals(
                    subscription.getCheckoutAttemptId(),
                    paymentData.checkoutAttemptId())) {

                throw new PaymentSynchronizationException();
            }

            return;
        }

        String checkoutSessionId =
                subscription.getStripeCheckoutSessionId();

        if (checkoutSessionId == null
                || !paymentWebhookGateway
                .checkoutBelongsToSubscription(
                        checkoutSessionId,
                        paymentData.stripeSubscriptionId())) {

            throw new PaymentSynchronizationException();
        }
    }
}
package com.nexoshift.petcarehub.subscriptions.infrastructure.payments.stripe;

import com.nexoshift.petcarehub.shared.domain.exceptions.BadRequestException;
import com.nexoshift.petcarehub.shared.domain.exceptions.ServiceUnavailableException;
import com.nexoshift.petcarehub.subscriptions.application.internal.outboundservices.payments.CheckoutSessionData;
import com.nexoshift.petcarehub.subscriptions.application.internal.outboundservices.payments.PaymentGateway;
import com.nexoshift.petcarehub.subscriptions.domain.exceptions.CheckoutUnavailableException;
import com.nexoshift.petcarehub.subscriptions.domain.exceptions.PaymentHistoryUnavailableException;
import com.nexoshift.petcarehub.subscriptions.domain.exceptions.SubscriptionCancellationNotAllowedException;
import com.nexoshift.petcarehub.subscriptions.domain.model.aggregates.Plan;
import com.nexoshift.petcarehub.subscriptions.domain.model.valueobjects.SubscriptionPayment;
import com.nexoshift.petcarehub.subscriptions.domain.model.valueobjects.SubscriptionPaymentPage;
import com.nexoshift.petcarehub.subscriptions.infrastructure.payments.stripe.configuration.StripeCheckoutProperties;
import com.stripe.StripeClient;
import com.stripe.exception.InvalidRequestException;
import com.stripe.exception.StripeException;
import com.stripe.model.Invoice;
import com.stripe.model.Price;
import com.stripe.model.checkout.Session;
import com.stripe.net.RequestOptions;
import com.stripe.param.InvoiceListParams;
import com.stripe.param.SubscriptionUpdateParams;
import com.stripe.param.checkout.SessionCreateParams;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Locale;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class StripePaymentGateway implements PaymentGateway {

    private final StripeCheckoutProperties properties;

    @Override
    public CheckoutSessionData createCheckoutSession(
            Long subscriptionId,
            Long accountId,
            Plan plan,
            String idempotencyKey) {

        String priceId = properties.resolvePriceId(plan.getCode());

        if (priceId == null || priceId.isBlank()) {
            throw new CheckoutUnavailableException();
        }

        StripeClient client = createStripeClient();

        Map<String, String> metadata = Map.of(
                "subscription_id", subscriptionId.toString(),
                "account_id", accountId.toString(),
                "plan_code", plan.getCode().name(),
                "checkout_attempt_id", idempotencyKey
        );

        SessionCreateParams params = SessionCreateParams.builder()

                .setMode(SessionCreateParams.Mode.SUBSCRIPTION)

                .setSuccessUrl(properties.getSuccessUrl())

                .setCancelUrl(properties.getCancelUrl())

                .setClientReferenceId(subscriptionId.toString())

                .addAllowedPaymentMethodType(
                        SessionCreateParams.AllowedPaymentMethodType.CARD)

                .setAdaptivePricing(
                        SessionCreateParams.AdaptivePricing.builder()
                                .setEnabled(false)
                                .build()
                )

                .addLineItem(
                        SessionCreateParams.LineItem.builder()
                                .setPrice(priceId)
                                .setQuantity(1L)
                                .build()
                )

                .putAllMetadata(metadata)

                .setSubscriptionData(
                        SessionCreateParams.SubscriptionData.builder()
                                .putAllMetadata(metadata)
                                .build()
                )

                .build();

        RequestOptions options = RequestOptions.builder()
                .setIdempotencyKey(idempotencyKey)
                .setMaxNetworkRetries(2)
                .build();

        try {

            Price price = client.v1().prices().retrieve(priceId);

            validatePrice(price, plan);

            Session session = client.v1()
                    .checkout()
                    .sessions()
                    .create(params, options);

            return toCheckoutSessionData(session);

        } catch (StripeException exception) {

            log.error(
                    "Stripe checkout creation failed. Subscription: {}, request: {}",
                    subscriptionId,
                    exception.getRequestId()
            );

            throw new CheckoutUnavailableException(exception);
        }
    }

    @Override
    public CheckoutSessionData retrieveCheckoutSession(
            String sessionId) {

        try {

            Session session = createStripeClient()
                    .v1()
                    .checkout()
                    .sessions()
                    .retrieve(sessionId);

            return toCheckoutSessionData(session);

        } catch (StripeException exception) {

            log.error(
                    "Stripe checkout retrieval failed. Request: {}",
                    exception.getRequestId()
            );

            throw new CheckoutUnavailableException(exception);
        }
    }

    private StripeClient createStripeClient() {

        String secretKey = properties.getSecretKey();

        if (secretKey == null || secretKey.isBlank()) {
            throw new CheckoutUnavailableException();
        }

        return StripeClient.builder()
                .setApiKey(secretKey)
                .setConnectTimeout(5000)
                .setReadTimeout(15000)
                .build();
    }

    private CheckoutSessionData toCheckoutSessionData(
            Session session) {

        return new CheckoutSessionData(
                session.getId(),
                session.getUrl(),
                Instant.ofEpochSecond(session.getExpiresAt()),
                session.getStatus()
        );
    }

    private void validatePrice(Price price, Plan plan) {

        long expectedAmount = plan.getMonthlyPrice()
                .movePointRight(2)
                .longValueExact();

        boolean valid = Boolean.TRUE.equals(price.getActive())

                && Long.valueOf(expectedAmount)
                .equals(price.getUnitAmount())

                && plan.getCurrency().toString()
                .equalsIgnoreCase(price.getCurrency())

                && price.getRecurring() != null

                && "month".equals(
                price.getRecurring().getInterval())

                && Long.valueOf(1L).equals(
                price.getRecurring().getIntervalCount());

        if (!valid) {

            log.error(
                    "Stripe price configuration does not match plan {}",
                    plan.getCode()
            );

            throw new CheckoutUnavailableException();
        }
    }

    @Override
    public void updateSubscriptionCancellation(
            String stripeSubscriptionId,
            boolean cancelAtPeriodEnd) {

        StripeClient client = createStripeClient();

        try {
            var stripeSubscription = client.v1()
                    .subscriptions()
                    .retrieve(stripeSubscriptionId);

            String status = stripeSubscription.getStatus();

            if (!"active".equals(status)
                    && !"past_due".equals(status)
                    && !"unpaid".equals(status)) {

                throw new SubscriptionCancellationNotAllowedException(
                        "The current Stripe subscription status "
                                + "does not allow cancellation updates."
                );
            }

            var params = SubscriptionUpdateParams.builder()
                    .setCancelAtPeriodEnd(cancelAtPeriodEnd)
                    .build();

            client.v1()
                    .subscriptions()
                    .update(stripeSubscriptionId, params);

        } catch (StripeException exception) {

            log.error(
                    "Stripe subscription cancellation update failed. Subscription: {}, request: {}",
                    stripeSubscriptionId,
                    exception.getRequestId()
            );

            throw new ServiceUnavailableException(
                    "Unable to update subscription cancellation in Stripe.",
                    exception
            );
        }
    }

    @Override
    public SubscriptionPaymentPage retrieveSubscriptionPayments(
            String stripeSubscriptionId,
            int limit,
            String startingAfter) {

        try {

            StripeClient client = createStripeClient();

            var paramsBuilder = InvoiceListParams.builder()
                    .setSubscription(stripeSubscriptionId)
                    .setStatus(InvoiceListParams.Status.PAID)
                    .setLimit((long) limit);

            if (startingAfter != null) {
                paramsBuilder.setStartingAfter(startingAfter);
            }

            var invoices = client.v1()
                    .invoices()
                    .list(paramsBuilder.build());

            var payments = invoices.getData()
                    .stream()
                    .filter(invoice ->
                            invoice.getAmountPaid() != null
                                    && invoice.getAmountPaid() > 0
                    )
                    .map(this::toSubscriptionPayment)
                    .toList();

            String nextCursor = null;

            if (Boolean.TRUE.equals(invoices.getHasMore())
                    && !invoices.getData().isEmpty()) {

                nextCursor = invoices.getData()
                        .getLast()
                        .getId();
            }

            return new SubscriptionPaymentPage(
                    payments,
                    nextCursor
            );

        } catch (StripeException exception) {

            if (exception instanceof InvalidRequestException invalidRequest
                    && "starting_after".equals(invalidRequest.getParam())) {

                throw new BadRequestException(
                        "Invalid payment history cursor."
                );
            }

            log.error(
                    "Stripe payment history retrieval failed. Subscription: {}, request: {}",
                    stripeSubscriptionId,
                    exception.getRequestId()
            );

            throw new PaymentHistoryUnavailableException(exception);

        } catch (CheckoutUnavailableException exception) {

            throw new PaymentHistoryUnavailableException(exception);
        }
    }

    private SubscriptionPayment toSubscriptionPayment(
            Invoice invoice) {

        Long paidAt = invoice.getStatusTransitions() != null
                ? invoice.getStatusTransitions().getPaidAt()
                : null;

        return new SubscriptionPayment(
                invoice.getId(),
                invoice.getNumber(),
                BigDecimal.valueOf(invoice.getAmountPaid(), 2),
                invoice.getCurrency().toUpperCase(Locale.ROOT),
                paidAt != null
                        ? Instant.ofEpochSecond(paidAt)
                        : null,
                invoice.getHostedInvoiceUrl(),
                invoice.getInvoicePdf()
        );
    }
}
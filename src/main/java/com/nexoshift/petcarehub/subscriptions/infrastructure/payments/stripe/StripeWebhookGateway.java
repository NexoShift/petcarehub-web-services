package com.nexoshift.petcarehub.subscriptions.infrastructure.payments.stripe;

import com.nexoshift.petcarehub.subscriptions.application.internal.outboundservices.payments.PaymentWebhookData;
import com.nexoshift.petcarehub.subscriptions.application.internal.outboundservices.payments.PaymentWebhookGateway;
import com.nexoshift.petcarehub.subscriptions.application.internal.outboundservices.payments.SubscriptionPaymentData;
import com.nexoshift.petcarehub.subscriptions.domain.exceptions.InvalidWebhookSignatureException;
import com.nexoshift.petcarehub.subscriptions.domain.exceptions.PaymentSynchronizationException;
import com.nexoshift.petcarehub.subscriptions.domain.model.valueobjects.PlanCode;
import com.nexoshift.petcarehub.subscriptions.domain.model.valueobjects.SubscriptionStatus;
import com.nexoshift.petcarehub.subscriptions.infrastructure.payments.stripe.configuration.StripeCheckoutProperties;
import com.stripe.StripeClient;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.Invoice;
import com.stripe.model.Subscription;
import com.stripe.model.SubscriptionItem;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class StripeWebhookGateway implements PaymentWebhookGateway {

    private final StripeCheckoutProperties properties;
    private final JsonMapper jsonMapper;

    private static final Set<String> SUPPORTED_EVENTS = Set.of(
            "checkout.session.completed",
            "checkout.session.expired",
            "invoice.paid",
            "invoice.payment_failed",
            "customer.subscription.created",
            "customer.subscription.updated",
            "customer.subscription.deleted"
    );

    @Override
    public Optional<PaymentWebhookData> verifyAndResolve(
            String payload,
            String signature) {

        Event event = verifyEvent(payload, signature);

        if (!SUPPORTED_EVENTS.contains(event.getType())) {
            return Optional.empty();
        }

        String objectId = extractObjectId(event);

        StripeClient client = createStripeClient();

        try {

            return switch (event.getType()) {

                case "checkout.session.completed",
                     "checkout.session.expired" ->

                        resolveCheckoutEvent(
                                client, event, objectId);

                case "invoice.paid",
                     "invoice.payment_failed" ->

                        resolveInvoiceEvent(
                                client, event, objectId);

                case "customer.subscription.created",
                     "customer.subscription.updated",
                     "customer.subscription.deleted" ->

                        resolveSubscriptionEvent(
                                client, event, objectId);

                default -> Optional.empty();
            };

        } catch (StripeException exception) {

            log.error(
                    "Unable to resolve Stripe event {}. Request: {}",
                    event.getId(),
                    exception.getRequestId()
            );

            throw new PaymentSynchronizationException(exception);
        }
    }

    @Override
    public SubscriptionPaymentData retrieveSubscriptionPaymentData(
            String stripeSubscriptionId,
            String invoiceId) {

        StripeClient client = createStripeClient();

        try {

            Subscription subscription = client.v1()
                    .subscriptions()
                    .retrieve(stripeSubscriptionId);

            Metadata metadata = extractMetadata(
                    subscription.getMetadata());

            if (metadata == null) {
                throw new PaymentSynchronizationException();
            }

            SubscriptionItem item = resolveSubscriptionItem(
                    subscription, metadata.planCode());

            PaidPeriod paidPeriod = retrievePaidPeriod(
                    client,
                    subscription.getLatestInvoice(),
                    subscription.getId(),
                    item.getId()
            );

            if (invoiceId != null
                    && !invoiceId.equals(
                    subscription.getLatestInvoice())) {

                PaidPeriod eventPeriod = retrievePaidPeriod(
                        client,
                        invoiceId,
                        subscription.getId(),
                        item.getId()
                );

                paidPeriod = latestPeriod(
                        paidPeriod, eventPeriod);
            }

            return new SubscriptionPaymentData(
                    metadata.subscriptionId(),
                    metadata.accountId(),
                    metadata.planCode(),
                    metadata.checkoutAttemptId(),
                    subscription.getCustomer(),
                    subscription.getId(),
                    mapStatus(subscription.getStatus()),
                    paidPeriod == null ? null : paidPeriod.start(),
                    paidPeriod == null ? null : paidPeriod.end(),
                    Boolean.TRUE.equals(
                            subscription.getCancelAtPeriodEnd())
            );

        } catch (StripeException exception) {

            log.error(
                    "Unable to synchronize Stripe subscription {}. Request: {}",
                    stripeSubscriptionId,
                    exception.getRequestId()
            );

            throw new PaymentSynchronizationException(exception);
        }
    }

    @Override
    public boolean checkoutBelongsToSubscription(
            String checkoutSessionId,
            String stripeSubscriptionId) {

        try {

            Session session = createStripeClient()
                    .v1()
                    .checkout()
                    .sessions()
                    .retrieve(checkoutSessionId);

            return "subscription".equals(session.getMode())
                    && Objects.equals(
                    session.getSubscription(),
                    stripeSubscriptionId
            );

        } catch (StripeException exception) {

            throw new PaymentSynchronizationException(exception);
        }
    }

    private Event verifyEvent(
            String payload,
            String signature) {

        String webhookSecret = properties.getWebhookSecret();

        if (webhookSecret == null || webhookSecret.isBlank()) {
            throw new PaymentSynchronizationException();
        }

        if (payload == null || payload.isBlank()
                || signature == null || signature.isBlank()) {

            throw new InvalidWebhookSignatureException();
        }

        try {

            return Webhook.constructEvent(
                    payload,
                    signature,
                    webhookSecret
            );

        } catch (SignatureVerificationException
                 | RuntimeException exception) {

            throw new InvalidWebhookSignatureException();
        }
    }

    private String extractObjectId(Event event) {

        try {

            String rawJson = event.getDataObjectDeserializer()
                    .getRawJson();

            JsonNode object = jsonMapper.readTree(rawJson);

            JsonNode idNode = object.path("id");

            if (!idNode.isString()) {
                throw new InvalidWebhookSignatureException();
            }

            String objectId = idNode.stringValue();

            if (objectId == null || objectId.isBlank()) {
                throw new InvalidWebhookSignatureException();
            }

            return objectId;

        } catch (RuntimeException exception) {

            throw new InvalidWebhookSignatureException();
        }
    }

    private Optional<PaymentWebhookData> resolveCheckoutEvent(
            StripeClient client,
            Event event,
            String sessionId) throws StripeException {

        Session session = client.v1()
                .checkout()
                .sessions()
                .retrieve(sessionId);

        if (!"subscription".equals(session.getMode())) {
            return Optional.empty();
        }

        Metadata metadata = extractMetadata(
                session.getMetadata());

        if (metadata == null) {
            return Optional.empty();
        }

        boolean expired = "checkout.session.expired"
                .equals(event.getType());

        if (expired) {

            if (!"expired".equals(session.getStatus())) {
                throw new PaymentSynchronizationException();
            }

        } else if (!"complete".equals(session.getStatus())
                || session.getSubscription() == null) {

            throw new PaymentSynchronizationException();
        }

        return Optional.of(new PaymentWebhookData(
                event.getId(),
                event.getType(),
                metadata.subscriptionId(),
                metadata.accountId(),
                metadata.planCode(),
                session.getSubscription(),
                null,
                session.getId(),
                metadata.checkoutAttemptId(),
                Instant.ofEpochSecond(session.getExpiresAt())
        ));
    }

    private Optional<PaymentWebhookData> resolveInvoiceEvent(
            StripeClient client,
            Event event,
            String invoiceId) throws StripeException {

        Invoice invoice = client.v1()
                .invoices()
                .retrieve(invoiceId);

        String stripeSubscriptionId =
                extractInvoiceSubscriptionId(invoice);

        if (stripeSubscriptionId == null) {
            return Optional.empty();
        }

        Subscription subscription = client.v1()
                .subscriptions()
                .retrieve(stripeSubscriptionId);

        return createWebhookData(
                event, subscription, invoiceId);
    }

    private Optional<PaymentWebhookData> resolveSubscriptionEvent(
            StripeClient client,
            Event event,
            String stripeSubscriptionId) throws StripeException {

        Subscription subscription = client.v1()
                .subscriptions()
                .retrieve(stripeSubscriptionId);

        return createWebhookData(
                event, subscription, null);
    }

    private Optional<PaymentWebhookData> createWebhookData(
            Event event,
            Subscription subscription,
            String invoiceId) {

        Metadata metadata = extractMetadata(
                subscription.getMetadata());

        if (metadata == null) {
            return Optional.empty();
        }

        return Optional.of(new PaymentWebhookData(
                event.getId(),
                event.getType(),
                metadata.subscriptionId(),
                metadata.accountId(),
                metadata.planCode(),
                subscription.getId(),
                invoiceId,
                null,
                metadata.checkoutAttemptId(),
                null
        ));
    }

    private String extractInvoiceSubscriptionId(
            Invoice invoice) {

        if (invoice.getParent() == null
                || invoice.getParent()
                .getSubscriptionDetails() == null) {

            return null;
        }

        return invoice.getParent()
                .getSubscriptionDetails()
                .getSubscription();
    }

    private SubscriptionItem resolveSubscriptionItem(
            Subscription subscription,
            PlanCode planCode) {

        String expectedPriceId =
                properties.resolvePriceId(planCode);

        if (subscription.getItems() == null
                || subscription.getItems().getData() == null
                || subscription.getItems().getData().size() != 1
                || Boolean.TRUE.equals(
                subscription.getItems().getHasMore())) {

            throw new PaymentSynchronizationException();
        }

        SubscriptionItem item = subscription.getItems()
                .getData()
                .getFirst();

        if (expectedPriceId == null
                || expectedPriceId.isBlank()
                || item.getPrice() == null
                || !expectedPriceId.equals(item.getPrice().getId())
                || !Long.valueOf(1L).equals(item.getQuantity())) {

            throw new PaymentSynchronizationException();
        }

        return item;
    }

    private PaidPeriod retrievePaidPeriod(
            StripeClient client,
            String invoiceId,
            String subscriptionId,
            String subscriptionItemId) throws StripeException {

        if (invoiceId == null) {
            return null;
        }

        Invoice invoice = client.v1()
                .invoices()
                .retrieve(invoiceId);

        if (!"paid".equals(invoice.getStatus())
                || !subscriptionId.equals(
                extractInvoiceSubscriptionId(invoice))) {

            return null;
        }

        if (!"subscription_create".equals(invoice.getBillingReason())
                && !"subscription_cycle".equals(
                invoice.getBillingReason())) {

            return null;
        }

        if (invoice.getLines() == null
                || invoice.getLines().getData() == null) {

            throw new PaymentSynchronizationException();
        }

        for (var line : invoice.getLines().getData()) {

            if (line.getParent() == null
                    || line.getPeriod() == null) {

                continue;
            }

            var details = line.getParent()
                    .getSubscriptionItemDetails();

            if (details == null
                    || Boolean.TRUE.equals(details.getProration())
                    || !subscriptionItemId.equals(
                    details.getSubscriptionItem())) {

                continue;
            }

            Instant start = Instant.ofEpochSecond(
                    line.getPeriod().getStart());

            Instant end = Instant.ofEpochSecond(
                    line.getPeriod().getEnd());

            return new PaidPeriod(start, end);
        }

        throw new PaymentSynchronizationException();
    }

    private PaidPeriod latestPeriod(
            PaidPeriod first,
            PaidPeriod second) {

        if (first == null) {
            return second;
        }

        if (second == null) {
            return first;
        }

        return second.end().isAfter(first.end())
                ? second
                : first;
    }

    private SubscriptionStatus mapStatus(String status) {

        return switch (status) {

            case "active" -> SubscriptionStatus.ACTIVE;

            case "past_due", "unpaid", "paused" ->
                    SubscriptionStatus.PAST_DUE;

            case "canceled" ->
                    SubscriptionStatus.CANCELED;

            case "incomplete_expired" ->
                    SubscriptionStatus.EXPIRED;

            case "incomplete", "trialing" ->
                    SubscriptionStatus.PENDING;

            default -> throw new PaymentSynchronizationException();
        };
    }

    private Metadata extractMetadata(
            Map<String, String> metadata) {

        if (metadata == null
                || (!metadata.containsKey("subscription_id")
                && !metadata.containsKey("account_id"))) {

            return null;
        }

        try {

            Long subscriptionId = Long.valueOf(
                    metadata.get("subscription_id"));

            Long accountId = Long.valueOf(
                    metadata.get("account_id"));

            PlanCode planCode = PlanCode.valueOf(
                    metadata.get("plan_code"));

            if (subscriptionId <= 0 || accountId <= 0) {
                throw new PaymentSynchronizationException();
            }

            return new Metadata(
                    subscriptionId,
                    accountId,
                    planCode,
                    metadata.get("checkout_attempt_id")
            );

        } catch (IllegalArgumentException
                 | NullPointerException exception) {

            throw new PaymentSynchronizationException(exception);
        }
    }

    private StripeClient createStripeClient() {

        String secretKey = properties.getSecretKey();

        if (secretKey == null || secretKey.isBlank()) {
            throw new PaymentSynchronizationException();
        }

        return StripeClient.builder()
                .setApiKey(secretKey)
                .setConnectTimeout(5000)
                .setReadTimeout(15000)
                .build();
    }

    private record Metadata(
            Long subscriptionId,
            Long accountId,
            PlanCode planCode,
            String checkoutAttemptId
    ) {}

    private record PaidPeriod(
            Instant start,
            Instant end
    ) {}
}
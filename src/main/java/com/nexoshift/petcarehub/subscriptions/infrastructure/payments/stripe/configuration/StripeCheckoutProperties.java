package com.nexoshift.petcarehub.subscriptions.infrastructure.payments.stripe.configuration;

import com.nexoshift.petcarehub.subscriptions.domain.model.valueobjects.PlanCode;
import lombok.Getter;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Stripe settings. They are required: the API does not start if one of them is missing
 * or does not look like a Stripe value, so a bad deployment fails at once instead of
 * failing later when a customer tries to pay.
 */
@Getter
@Component
public class StripeCheckoutProperties implements InitializingBean {

    @Value("${stripe.secret-key}")
    private String secretKey;

    @Value("${stripe.checkout.success-url}")
    private String successUrl;

    @Value("${stripe.checkout.cancel-url}")
    private String cancelUrl;

    @Value("${stripe.prices.pet-owner-basic}")
    private String petOwnerBasicPriceId;

    @Value("${stripe.prices.pet-owner-premium}")
    private String petOwnerPremiumPriceId;

    @Value("${stripe.prices.clinic-basic}")
    private String clinicBasicPriceId;

    @Value("${stripe.prices.clinic-premium}")
    private String clinicPremiumPriceId;

    @Value("${stripe.webhook.secret}")
    private String webhookSecret;

    public String resolvePriceId(PlanCode planCode) {

        return switch (planCode) {

            case PET_OWNER_BASIC -> petOwnerBasicPriceId;

            case PET_OWNER_PREMIUM -> petOwnerPremiumPriceId;

            case CLINIC_BASIC -> clinicBasicPriceId;

            case CLINIC_PREMIUM -> clinicPremiumPriceId;
        };
    }

    /** Runs when Spring creates the bean: stops the startup with every problem found. */
    @Override
    public void afterPropertiesSet() {
        List<String> problems = new ArrayList<>();
        check(problems, "STRIPE_SECRET_KEY", secretKey, "sk_", "rk_");
        check(problems, "STRIPE_PRICE_PET_OWNER_BASIC", petOwnerBasicPriceId, "price_");
        check(problems, "STRIPE_PRICE_PET_OWNER_PREMIUM", petOwnerPremiumPriceId, "price_");
        check(problems, "STRIPE_PRICE_CLINIC_BASIC", clinicBasicPriceId, "price_");
        check(problems, "STRIPE_PRICE_CLINIC_PREMIUM", clinicPremiumPriceId, "price_");
        check(problems, "STRIPE_WEBHOOK_SECRET", webhookSecret, "whsec_");
        check(problems, "STRIPE_CHECKOUT_SUCCESS_URL", successUrl, "http://", "https://");
        check(problems, "STRIPE_CHECKOUT_CANCEL_URL", cancelUrl, "http://", "https://");
        if (!problems.isEmpty()) {
            throw new IllegalStateException("Stripe is not configured correctly: "
                    + String.join("; ", problems));
        }
    }

    private static void check(List<String> problems, String variable, String value,
                              String... prefixes) {
        if (value == null || value.isBlank()) {
            problems.add(variable + " is empty");
            return;
        }
        for (String prefix : prefixes) {
            if (value.startsWith(prefix)) return;
        }
        problems.add(variable + " must start with " + String.join(" or ", prefixes));
    }
}

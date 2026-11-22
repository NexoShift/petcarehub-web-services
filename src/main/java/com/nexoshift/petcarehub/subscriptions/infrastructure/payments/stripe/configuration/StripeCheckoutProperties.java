package com.nexoshift.petcarehub.subscriptions.infrastructure.payments.stripe.configuration;

import com.nexoshift.petcarehub.subscriptions.domain.model.valueobjects.PlanCode;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Getter
@Component
public class StripeCheckoutProperties {

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
}
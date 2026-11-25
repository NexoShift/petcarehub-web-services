package com.nexoshift.petcarehub.subscriptions.interfaces.rest.resources;

public record SubscriptionEntitlementsResource(
        Long subscriptionId,
        Long accountId,
        String planCode,
        Integer maxPets,
        Integer maxVeterinarians,
        int monthlyComputerVisionCredits,
        int monthlyLlmCredits
) {
}
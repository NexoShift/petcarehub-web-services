package com.nexoshift.petcarehub.subscriptions.application.acl.services;

import com.nexoshift.petcarehub.subscriptions.domain.model
        .commands.ConfirmAiCreditReservationCommand;
import com.nexoshift.petcarehub.subscriptions.domain.model
        .commands.ReleaseAiCreditReservationCommand;
import com.nexoshift.petcarehub.subscriptions.domain.model
        .commands.ReserveAiCreditCommand;
import com.nexoshift.petcarehub.subscriptions.domain.model
        .valueobjects.AiCreditType;
import com.nexoshift.petcarehub.subscriptions.domain.services
        .AiCreditCommandService;
import com.nexoshift.petcarehub.subscriptions.domain.services
        .SubscriptionEntitlementService;
import com.nexoshift.petcarehub.subscriptions.interfaces.acl
        .SubscriptionContextFacade;

import org.springframework.stereotype.Service;

@Service
public class SubscriptionContextFacadeImpl
        implements SubscriptionContextFacade {

    private final AiCreditCommandService aiCreditCommandService;
    private final SubscriptionEntitlementService subscriptionEntitlementService;

    public SubscriptionContextFacadeImpl(
            AiCreditCommandService aiCreditCommandService,
            SubscriptionEntitlementService subscriptionEntitlementService) {

        this.aiCreditCommandService = aiCreditCommandService;
        this.subscriptionEntitlementService =
                subscriptionEntitlementService;
    }

    @Override
    public Long reserveComputerVisionCredit(Long requesterUserId) {

        return aiCreditCommandService.handle(
                new ReserveAiCreditCommand(
                        requesterUserId,
                        AiCreditType.COMPUTER_VISION
                )
        );
    }

    @Override
    public Long reserveLlmCredit(Long requesterUserId) {

        return aiCreditCommandService.handle(
                new ReserveAiCreditCommand(
                        requesterUserId,
                        AiCreditType.LLM
                )
        );
    }

    @Override
    public void confirmAiCreditReservation(Long reservationId) {

        aiCreditCommandService.handle(
                new ConfirmAiCreditReservationCommand(reservationId)
        );
    }

    @Override
    public void releaseAiCreditReservation(Long reservationId) {

        aiCreditCommandService.handle(
                new ReleaseAiCreditReservationCommand(reservationId)
        );
    }

    @Override
    public void ensureActiveSubscription(Long accountId) {

        subscriptionEntitlementService
                .ensureActiveSubscription(accountId);
    }

    @Override
    public void lockActiveSubscriptionForRegistration(Long accountId) {

        subscriptionEntitlementService
                .lockActiveSubscriptionForRegistration(accountId);
    }

    @Override
    public void ensurePetRegistrationAllowed(
            Long accountId,
            long registeredPets) {

        subscriptionEntitlementService.ensurePetRegistrationAllowed(
                accountId,
                registeredPets
        );
    }

    @Override
    public void ensureVeterinarianRegistrationAllowed(
            Long accountId,
            long registeredVeterinarians) {

        subscriptionEntitlementService
                .ensureVeterinarianRegistrationAllowed(
                        accountId,
                        registeredVeterinarians
                );
    }
}
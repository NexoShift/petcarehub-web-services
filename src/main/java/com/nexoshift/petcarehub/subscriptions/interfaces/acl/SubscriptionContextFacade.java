package com.nexoshift.petcarehub.subscriptions.interfaces.acl;

/**
 * Published contract of the Subscriptions bounded context.
 */
public interface SubscriptionContextFacade {

    /**
     * Reserves a computer vision credit for the requesting user's account.
     */
    Long reserveComputerVisionCredit(Long requesterUserId);

    /**
     * Reserves an LLM credit for the requesting user's account.
     */
    Long reserveLlmCredit(Long requesterUserId);

    /**
     * Confirms a previously reserved AI credit.
     */
    void confirmAiCreditReservation(Long reservationId);

    /**
     * Releases a previously reserved AI credit.
     */
    void releaseAiCreditReservation(Long reservationId);

    /**
     * Ensures that the account has an active subscription.
     */
    void ensureActiveSubscription(Long accountId);

    /**
     * Locks the active subscription before counting registered resources.
     * Must be called within the registration transaction.
     */
    void lockActiveSubscriptionForRegistration(Long accountId);

    /**
     * Validates whether the subscription allows an additional pet.
     * Must be called after locking the subscription and counting pets.
     */
    void ensurePetRegistrationAllowed(
            Long accountId,
            long registeredPets
    );

    /**
     * Validates whether the subscription allows an additional veterinarian.
     * Must be called after locking the subscription and counting veterinarians.
     */
    void ensureVeterinarianRegistrationAllowed(
            Long accountId,
            long registeredVeterinarians
    );
}
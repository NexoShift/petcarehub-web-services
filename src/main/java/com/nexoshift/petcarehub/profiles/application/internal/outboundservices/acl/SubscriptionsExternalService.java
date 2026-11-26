package com.nexoshift.petcarehub.profiles.application.internal.outboundservices.acl;

import com.nexoshift.petcarehub.subscriptions.interfaces.acl.SubscriptionContextFacade;
import org.springframework.stereotype.Service;

/**
 * Subscriptions External Service
 * <p>
 *     Anti-corruption layer that lets the Profiles bounded context check
 *     the registration limits of the account's subscription plan through
 *     the Subscriptions facade.
 * </p>
 * <p>
 *     Both methods must run inside the registration transaction: the
 *     subscription row is locked first, then the registered resources are
 *     counted, so two concurrent registrations cannot exceed the limit.
 * </p>
 */
@Service
public class SubscriptionsExternalService {

    private final SubscriptionContextFacade subscriptionContextFacade;

    public SubscriptionsExternalService(SubscriptionContextFacade subscriptionContextFacade) {
        this.subscriptionContextFacade = subscriptionContextFacade;
    }

    /**
     * Locks the account's active subscription. Fails with 403 when there is none.
     *
     * @param accountId the IAM account identifier
     */
    public void lockActiveSubscription(Long accountId) {
        subscriptionContextFacade.lockActiveSubscriptionForRegistration(accountId);
    }

    /**
     * Validates that the plan allows one more pet. Fails with 409 when the limit is reached.
     *
     * @param accountId the IAM account identifier
     * @param registeredPets the pets already registered by the account
     */
    public void ensurePetRegistrationAllowed(Long accountId, long registeredPets) {
        subscriptionContextFacade.ensurePetRegistrationAllowed(accountId, registeredPets);
    }

    /**
     * Validates that the plan allows one more veterinarian. Fails with 409 when the limit is reached.
     *
     * @param accountId the IAM account identifier
     * @param registeredVeterinarians the veterinarians already registered by the account
     */
    public void ensureVeterinarianRegistrationAllowed(Long accountId, long registeredVeterinarians) {
        subscriptionContextFacade.ensureVeterinarianRegistrationAllowed(accountId, registeredVeterinarians);
    }
}

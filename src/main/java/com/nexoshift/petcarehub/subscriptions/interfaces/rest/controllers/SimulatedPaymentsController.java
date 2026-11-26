package com.nexoshift.petcarehub.subscriptions.interfaces.rest.controllers;

import com.nexoshift.petcarehub.subscriptions.domain.model.commands.SimulateSubscriptionPaymentCommand;
import com.nexoshift.petcarehub.subscriptions.domain.services.SimulatedPaymentCommandService;
import com.nexoshift.petcarehub.subscriptions.interfaces.rest.resources.SubscriptionResource;
import com.nexoshift.petcarehub.subscriptions.interfaces.rest.transform.SubscriptionResourceFromEntityAssembler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Development only: pays the pending subscription without Stripe, so the seed script,
 * demos and tests can use the platform. It does not exist unless
 * subscriptions.simulated-payments.enabled=true. Keep it false in production.
 */
@RestController
@RequestMapping(
        value = "/api/v1/accounts/{accountId}/subscription/simulated-payments",
        produces = MediaType.APPLICATION_JSON_VALUE
)
@ConditionalOnProperty(name = "subscriptions.simulated-payments.enabled", havingValue = "true")
@PreAuthorize("hasAnyRole('PET_OWNER', 'CLINIC_ADMIN')")
@Tag(name = "Subscriptions", description = "Available subscription endpoints")
public class SimulatedPaymentsController {

    private final SimulatedPaymentCommandService simulatedPaymentCommandService;

    public SimulatedPaymentsController(SimulatedPaymentCommandService simulatedPaymentCommandService) {
        this.simulatedPaymentCommandService = simulatedPaymentCommandService;
    }

    @PostMapping
    @Operation(
            summary = "Simulate the payment of the subscription (development only)",
            description = "Marks the account's subscription as ACTIVE for 30 days, as the Stripe webhook does"
    )
    public ResponseEntity<SubscriptionResource> simulatePayment(@PathVariable Long accountId) {
        var subscription = simulatedPaymentCommandService.handle(
                new SimulateSubscriptionPaymentCommand(accountId));
        return ResponseEntity.ok(
                SubscriptionResourceFromEntityAssembler.toResourceFromEntity(subscription));
    }
}

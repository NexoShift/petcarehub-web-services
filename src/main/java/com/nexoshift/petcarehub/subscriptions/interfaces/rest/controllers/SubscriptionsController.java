package com.nexoshift.petcarehub.subscriptions.interfaces.rest.controllers;

import com.nexoshift.petcarehub.subscriptions.domain.exceptions.SubscriptionNotFoundException;
import com.nexoshift.petcarehub.subscriptions.domain.model.queries.GetSubscriptionByAccountIdQuery;
import com.nexoshift.petcarehub.subscriptions.domain.services.SubscriptionCommandService;
import com.nexoshift.petcarehub.subscriptions.domain.services.SubscriptionQueryService;
import com.nexoshift.petcarehub.subscriptions.interfaces.rest.resources.CreateSubscriptionResource;
import com.nexoshift.petcarehub.subscriptions.interfaces.rest.resources.SubscriptionResource;
import com.nexoshift.petcarehub.subscriptions.interfaces.rest.transform.CreateSubscriptionCommandFromResourceAssembler;
import com.nexoshift.petcarehub.subscriptions.interfaces.rest.transform.SubscriptionResourceFromEntityAssembler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.nexoshift.petcarehub.subscriptions.domain.model.commands.CreateCheckoutSessionCommand;
import com.nexoshift.petcarehub.subscriptions.interfaces.rest.resources.CheckoutSessionResource;
import com.nexoshift.petcarehub.subscriptions.interfaces.rest.transform.CheckoutSessionResourceFromEntityAssembler;

/**
 * REST controller for managing account subscriptions.
 */
@RestController
@RequestMapping(
        value = "/api/v1/accounts/{accountId}",
        produces = MediaType.APPLICATION_JSON_VALUE
)
@PreAuthorize("hasAnyRole('PET_OWNER', 'CLINIC_ADMIN')")
@Tag(name = "Subscriptions", description = "Available subscription endpoints")
public class SubscriptionsController {

    private final SubscriptionCommandService subscriptionCommandService;
    private final SubscriptionQueryService subscriptionQueryService;

    public SubscriptionsController(
            SubscriptionCommandService subscriptionCommandService,
            SubscriptionQueryService subscriptionQueryService) {

        this.subscriptionCommandService = subscriptionCommandService;
        this.subscriptionQueryService = subscriptionQueryService;
    }

    /**
     * Creates a pending subscription for an account.
     */
    @PostMapping(
            value = "/subscriptions",
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    @Operation(summary = "Create a subscription")
    public ResponseEntity<SubscriptionResource> createSubscription(
            @PathVariable Long accountId,
            @Valid @RequestBody CreateSubscriptionResource resource) {

        var command = CreateSubscriptionCommandFromResourceAssembler
                .toCommandFromResource(accountId, resource);

        subscriptionCommandService.handle(command);

        var subscription = subscriptionQueryService.handle(
                new GetSubscriptionByAccountIdQuery(accountId)
        ).orElseThrow(() ->
                new SubscriptionNotFoundException(accountId));

        var subscriptionResource =
                SubscriptionResourceFromEntityAssembler
                        .toResourceFromEntity(subscription);

        return new ResponseEntity<>(
                subscriptionResource,
                HttpStatus.CREATED
        );
    }

    /**
     * Retrieves the subscription associated with an account.
     */
    @GetMapping("/subscription")
    @Operation(summary = "Get subscription by account ID")
    public ResponseEntity<SubscriptionResource> getSubscriptionByAccountId(
            @PathVariable Long accountId) {

        var subscription = subscriptionQueryService.handle(
                new GetSubscriptionByAccountIdQuery(accountId)
        ).orElseThrow(() ->
                new SubscriptionNotFoundException(accountId));

        var resource = SubscriptionResourceFromEntityAssembler
                .toResourceFromEntity(subscription);

        return ResponseEntity.ok(resource);
    }

    @PostMapping("/subscription/checkout-sessions")
    @Operation(
            summary = "Create a checkout session",
            description = "Creates or retrieves a checkout session "
                    + "for the account's pending subscription"
    )
    public ResponseEntity<CheckoutSessionResource> createCheckoutSession(
            @PathVariable Long accountId) {

        var command = new CreateCheckoutSessionCommand(accountId);

        var subscription = subscriptionCommandService.handle(command);

        var resource = CheckoutSessionResourceFromEntityAssembler
                .toResourceFromEntity(subscription);

        return ResponseEntity.ok(resource);
    }
}
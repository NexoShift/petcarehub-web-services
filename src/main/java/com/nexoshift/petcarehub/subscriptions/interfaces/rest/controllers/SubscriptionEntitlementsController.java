package com.nexoshift.petcarehub.subscriptions.interfaces.rest.controllers;

import com.nexoshift.petcarehub.subscriptions.domain.model.queries
        .GetSubscriptionEntitlementsByAccountIdQuery;
import com.nexoshift.petcarehub.subscriptions.domain.services
        .SubscriptionEntitlementService;
import com.nexoshift.petcarehub.subscriptions.interfaces.rest.resources
        .SubscriptionEntitlementsResource;
import com.nexoshift.petcarehub.subscriptions.interfaces.rest.transform
        .SubscriptionEntitlementsResourceFromEntityAssembler;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(
        value = "/api/v1/accounts/{accountId}/subscription/entitlements",
        produces = MediaType.APPLICATION_JSON_VALUE
)
@Tag(
        name = "Subscription Entitlements",
        description = "Available subscription entitlement endpoints"
)
public class SubscriptionEntitlementsController {

    private final SubscriptionEntitlementService subscriptionEntitlementService;

    public SubscriptionEntitlementsController(
            SubscriptionEntitlementService subscriptionEntitlementService) {

        this.subscriptionEntitlementService =
                subscriptionEntitlementService;
    }

    @GetMapping
    @PreAuthorize(
            "hasAnyRole('PET_OWNER', 'CLINIC_ADMIN', 'VETERINARIAN')"
    )
    @Operation(
            summary = "Get subscription entitlements",
            description = "Retrieve the benefits and limits of the active subscription"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Subscription entitlements retrieved successfully"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid account identifier"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Access denied or no active subscription"
            )
    })
    public ResponseEntity<SubscriptionEntitlementsResource>
    getSubscriptionEntitlements(@PathVariable Long accountId) {

        var query =
                new GetSubscriptionEntitlementsByAccountIdQuery(accountId);

        var subscription = subscriptionEntitlementService.handle(query);

        var resource = SubscriptionEntitlementsResourceFromEntityAssembler
                .toResourceFromEntity(subscription);

        return ResponseEntity.ok(resource);
    }
}
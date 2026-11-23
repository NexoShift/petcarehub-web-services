package com.nexoshift.petcarehub.subscriptions.interfaces.rest.controllers;

import com.nexoshift.petcarehub.subscriptions.domain.services
        .SubscriptionCancellationCommandService;
import com.nexoshift.petcarehub.subscriptions.interfaces.rest.resources
        .UpdateSubscriptionCancellationResource;
import com.nexoshift.petcarehub.subscriptions.interfaces.rest.transform
        .UpdateSubscriptionCancellationCommandFromResourceAssembler;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(
        value = "/api/v1/accounts/{accountId}/subscription",
        produces = MediaType.APPLICATION_JSON_VALUE
)
@Tag(
        name = "Subscriptions",
        description = "Available subscription endpoints"
)
public class SubscriptionCancellationController {

    private final SubscriptionCancellationCommandService
            subscriptionCancellationCommandService;

    public SubscriptionCancellationController(
            SubscriptionCancellationCommandService
                    subscriptionCancellationCommandService) {

        this.subscriptionCancellationCommandService =
                subscriptionCancellationCommandService;
    }

    @PatchMapping
    @PreAuthorize("hasAnyRole('PET_OWNER', 'CLINIC_ADMIN')")
    @Operation(
            summary = "Update subscription cancellation",
            description = "Schedule or revert a subscription cancellation"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "202",
                    description = "Subscription cancellation update accepted"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid cancellation request"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Access denied"
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Subscription cancellation update not allowed"
            ),
            @ApiResponse(
                    responseCode = "503",
                    description = "Stripe service unavailable"
            )
    })
    public ResponseEntity<Void> updateSubscriptionCancellation(
            @PathVariable Long accountId,
            @Valid @RequestBody
            UpdateSubscriptionCancellationResource resource) {

        var command =
                UpdateSubscriptionCancellationCommandFromResourceAssembler
                        .toCommandFromResource(accountId, resource);

        subscriptionCancellationCommandService.handle(command);

        return ResponseEntity.accepted().build();
    }
}
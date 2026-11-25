package com.nexoshift.petcarehub.subscriptions.interfaces.rest.controllers;

import com.nexoshift.petcarehub.subscriptions.domain.model.queries
        .GetSubscriptionPaymentsByAccountIdQuery;
import com.nexoshift.petcarehub.subscriptions.domain.services
        .SubscriptionPaymentQueryService;
import com.nexoshift.petcarehub.subscriptions.interfaces.rest.resources
        .SubscriptionPaymentPageResource;
import com.nexoshift.petcarehub.subscriptions.interfaces.rest.transform
        .SubscriptionPaymentPageResourceAssembler;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(
        value = "/api/v1/accounts/{accountId}/subscription/payments",
        produces = MediaType.APPLICATION_JSON_VALUE
)
@Tag(
        name = "Subscription Payments",
        description = "Available subscription payment history endpoints"
)
public class SubscriptionPaymentsController {

    private final SubscriptionPaymentQueryService
            subscriptionPaymentQueryService;

    public SubscriptionPaymentsController(
            SubscriptionPaymentQueryService subscriptionPaymentQueryService) {

        this.subscriptionPaymentQueryService =
                subscriptionPaymentQueryService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('PET_OWNER', 'CLINIC_ADMIN')")
    @Operation(
            summary = "Get subscription payment history",
            description = "Retrieve paid invoices associated with the subscription"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Payment history retrieved successfully"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid pagination parameters"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Access denied"
            ),
            @ApiResponse(
                    responseCode = "503",
                    description = "Payment history unavailable"
            )
    })
    public ResponseEntity<SubscriptionPaymentPageResource>
    getSubscriptionPayments(
            @PathVariable Long accountId,
            @RequestParam(defaultValue = "20") int limit,
            @RequestParam(required = false) String startingAfter) {

        var query = new GetSubscriptionPaymentsByAccountIdQuery(
                accountId,
                limit,
                startingAfter
        );

        var payments = subscriptionPaymentQueryService.handle(query);

        var resource = SubscriptionPaymentPageResourceAssembler
                .toResource(payments);

        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .body(resource);
    }
}
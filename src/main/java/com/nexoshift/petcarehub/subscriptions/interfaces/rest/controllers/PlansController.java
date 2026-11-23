package com.nexoshift.petcarehub.subscriptions.interfaces.rest.controllers;

import com.nexoshift.petcarehub.subscriptions.domain.exceptions.PlanNotFoundException;
import com.nexoshift.petcarehub.subscriptions.domain.model.queries.GetPlanByIdQuery;
import com.nexoshift.petcarehub.subscriptions.domain.model.queries.GetPlansQuery;
import com.nexoshift.petcarehub.subscriptions.domain.model.valueobjects.SubscriberType;
import com.nexoshift.petcarehub.subscriptions.domain.services.PlanQueryService;
import com.nexoshift.petcarehub.subscriptions.interfaces.rest.resources.PlanResource;
import com.nexoshift.petcarehub.subscriptions.interfaces.rest.transform.PlanResourceFromEntityAssembler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for retrieving subscription plans.
 */
@RestController
@RequestMapping(
        value = "/api/v1/plans",
        produces = MediaType.APPLICATION_JSON_VALUE
)
@Tag(
        name = "Subscription Plans",
        description = "Available subscription plan endpoints"
)
public class PlansController {

    private final PlanQueryService planQueryService;

    /**
     * Constructs the subscription plans controller.
     *
     * @param planQueryService the plan query service
     */
    public PlansController(PlanQueryService planQueryService) {
        this.planQueryService = planQueryService;
    }

    /**
     * Retrieves active plans, optionally filtered by subscriber type.
     *
     * @param subscriberType the optional subscriber type
     * @return the matching subscription plans
     */
    @GetMapping
    @Operation(
            summary = "Get subscription plans",
            description = "Retrieve active plans, optionally filtered by subscriber type"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Plans retrieved successfully"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid subscriber type"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized"
            )
    })
    public ResponseEntity<List<PlanResource>> getPlans(
            @RequestParam(required = false)
            SubscriberType subscriberType) {

        var query = new GetPlansQuery(subscriberType);

        var plans = planQueryService.handle(query);

        var resources = plans.stream()
                .map(PlanResourceFromEntityAssembler::toResourceFromEntity)
                .toList();

        return ResponseEntity.ok(resources);
    }

    /**
     * Retrieves an active subscription plan by its identifier.
     *
     * @param planId the plan identifier
     * @return the subscription plan
     */
    @GetMapping("/{planId}")
    @Operation(
            summary = "Get subscription plan by ID",
            description = "Retrieve an active subscription plan by its identifier"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Plan retrieved successfully"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid plan ID"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Plan not found"
            )
    })
    public ResponseEntity<PlanResource> getPlanById(
            @PathVariable Long planId) {

        var query = new GetPlanByIdQuery(planId);

        var plan = planQueryService.handle(query)
                .orElseThrow(() -> new PlanNotFoundException(planId));

        var resource = PlanResourceFromEntityAssembler
                .toResourceFromEntity(plan);

        return ResponseEntity.ok(resource);
    }
}
package com.nexoshift.petcarehub.subscriptions.interfaces.rest.controllers;

import com.nexoshift.petcarehub.subscriptions.domain.model.queries.GetAiCreditsByAccountIdQuery;
import com.nexoshift.petcarehub.subscriptions.domain.services.AiCreditQueryService;
import com.nexoshift.petcarehub.subscriptions.interfaces.rest.resources.AiCreditResource;
import com.nexoshift.petcarehub.subscriptions.interfaces.rest.transform.AiCreditResourceFromEntityAssembler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping(
        value = "/api/v1/accounts/{accountId}/subscription/ai-credits",
        produces = MediaType.APPLICATION_JSON_VALUE
)
@PreAuthorize("hasAnyRole('PET_OWNER', 'CLINIC_ADMIN', 'VETERINARIAN')")
@Tag(
        name = "AI Credits",
        description = "Subscription AI credit management endpoints"
)
public class AiCreditsController {

    private final AiCreditQueryService aiCreditQueryService;

    @GetMapping
    @Operation(
            summary = "Get AI credits by account ID",
            description = "Returns available, reserved and consumed "
                    + "AI credits for the current billing period"
    )
    public ResponseEntity<AiCreditResource> getAiCreditsByAccountId(
            @PathVariable Long accountId) {

        var query = new GetAiCreditsByAccountIdQuery(accountId);

        var creditPeriod = aiCreditQueryService.handle(query);

        var resource = AiCreditResourceFromEntityAssembler
                .toResourceFromEntity(creditPeriod);

        return ResponseEntity.ok(resource);
    }
}
package com.nexoshift.petcarehub.subscriptions.interfaces.rest;

import com.nexoshift.petcarehub.subscriptions.domain.model.commands.ProcessPaymentWebhookCommand;
import com.nexoshift.petcarehub.subscriptions.domain.services.PaymentWebhookCommandService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping(
        value = "/api/v1/subscriptions/webhooks/stripe",
        produces = MediaType.APPLICATION_JSON_VALUE
)
@Tag(
        name = "Stripe Webhooks",
        description = "Stripe payment notification endpoints"
)
public class StripeWebhooksController {

    private final PaymentWebhookCommandService
            paymentWebhookCommandService;

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "Process Stripe webhook",
            description = "Processes authenticated Stripe payment "
                    + "and subscription events"
    )
    public ResponseEntity<Void> processWebhook(

            @RequestBody String payload,

            @RequestHeader(
                    value = "Stripe-Signature",
                    required = false
            ) String signature) {

        var command = new ProcessPaymentWebhookCommand(
                payload,
                signature
        );

        paymentWebhookCommandService.handle(command);

        return ResponseEntity.ok().build();
    }
}
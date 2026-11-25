package com.nexoshift.petcarehub.subscriptions.domain.services;

import com.nexoshift.petcarehub.subscriptions.domain.model.commands.ProcessPaymentWebhookCommand;

public interface PaymentWebhookCommandService {

    void handle(ProcessPaymentWebhookCommand command);
}
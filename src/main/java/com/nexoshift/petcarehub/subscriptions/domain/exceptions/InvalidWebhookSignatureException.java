package com.nexoshift.petcarehub.subscriptions.domain.exceptions;

import com.nexoshift.petcarehub.shared.domain.exceptions.BadRequestException;

public class InvalidWebhookSignatureException
        extends BadRequestException {

    public InvalidWebhookSignatureException() {
        super("Invalid payment webhook signature or payload");
    }
}
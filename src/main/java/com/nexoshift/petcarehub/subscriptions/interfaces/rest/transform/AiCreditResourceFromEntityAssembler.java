package com.nexoshift.petcarehub.subscriptions.interfaces.rest.transform;

import com.nexoshift.petcarehub.subscriptions.domain.model.aggregates.AiCreditPeriod;
import com.nexoshift.petcarehub.subscriptions.interfaces.rest.resources.AiCreditResource;

public class AiCreditResourceFromEntityAssembler {

    public static AiCreditResource toResourceFromEntity(
            AiCreditPeriod entity) {

        var computerVision = new AiCreditResource.CreditBalance(
                entity.getComputerVisionLimit(),
                entity.getComputerVisionReserved(),
                entity.getComputerVisionConsumed(),
                entity.getComputerVisionAvailable()
        );

        var llm = new AiCreditResource.CreditBalance(
                entity.getLlmLimit(),
                entity.getLlmReserved(),
                entity.getLlmConsumed(),
                entity.getLlmAvailable()
        );

        return new AiCreditResource(
                entity.getSubscriptionId(),
                entity.getAccountId(),
                entity.getPeriodStart(),
                entity.getPeriodEnd(),
                computerVision,
                llm
        );
    }
}
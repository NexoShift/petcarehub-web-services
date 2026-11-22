package com.nexoshift.petcarehub.subscriptions.application.internal.eventhandlers;

import com.nexoshift.petcarehub.subscriptions.domain.model.commands.InitializeAiCreditPeriodCommand;
import com.nexoshift.petcarehub.subscriptions.domain.model.events.SubscriptionPeriodPaidEvent;
import com.nexoshift.petcarehub.subscriptions.domain.services.AiCreditCommandService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class SubscriptionPeriodPaidEventHandler {

    private final AiCreditCommandService aiCreditCommandService;

    @EventListener
    @Transactional(propagation = Propagation.MANDATORY)
    public void on(SubscriptionPeriodPaidEvent event) {

        var command = new InitializeAiCreditPeriodCommand(
                event.subscriptionId(),
                event.accountId(),
                event.periodStart(),
                event.periodEnd()
        );

        aiCreditCommandService.handle(command);

        log.info(
                "AI credit period initialization handled for subscription {}",
                event.subscriptionId()
        );
    }
}
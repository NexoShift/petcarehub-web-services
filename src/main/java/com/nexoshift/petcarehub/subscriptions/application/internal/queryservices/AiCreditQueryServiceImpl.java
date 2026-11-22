package com.nexoshift.petcarehub.subscriptions.application.internal.queryservices;

import com.nexoshift.petcarehub.subscriptions.application.internal.outboundservices.acl.ExternalIamService;
import com.nexoshift.petcarehub.subscriptions.domain.exceptions.ActiveSubscriptionRequiredException;
import com.nexoshift.petcarehub.subscriptions.domain.exceptions.SubscriptionAccessDeniedException;
import com.nexoshift.petcarehub.subscriptions.domain.model.aggregates.AiCreditPeriod;
import com.nexoshift.petcarehub.subscriptions.domain.model.queries.GetAiCreditsByAccountIdQuery;
import com.nexoshift.petcarehub.subscriptions.domain.services.AiCreditQueryService;
import com.nexoshift.petcarehub.subscriptions.infrastructure.persistence.jpa.repositories.AiCreditPeriodRepository;
import com.nexoshift.petcarehub.subscriptions.infrastructure.persistence.jpa.repositories.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
@RequiredArgsConstructor
public class AiCreditQueryServiceImpl
        implements AiCreditQueryService {

    private final SubscriptionRepository subscriptionRepository;

    private final AiCreditPeriodRepository aiCreditPeriodRepository;

    private final ExternalIamService externalIamService;

    @Override
    @Transactional(readOnly = true)
    public AiCreditPeriod handle(GetAiCreditsByAccountIdQuery query) {

        validateAccountAccess(query.accountId());

        var subscription = subscriptionRepository
                .findByAccountId(query.accountId())
                .orElseThrow(ActiveSubscriptionRequiredException::new);

        if (!subscription.isActive()) {
            throw new ActiveSubscriptionRequiredException();
        }

        return aiCreditPeriodRepository
                .findBySubscriptionIdAndPeriodStart(
                        subscription.getId(),
                        subscription.getCurrentPeriodStart())
                .orElseGet(() -> new AiCreditPeriod(subscription));
    }

    private void validateAccountAccess(Long accountId) {

        Long requesterUserId =
                externalIamService.fetchAuthenticatedUserId();

        if (requesterUserId == null || requesterUserId <= 0) {
            throw new SubscriptionAccessDeniedException();
        }

        Long requesterAccountId = externalIamService
                .fetchAccountIdByUserId(requesterUserId);

        if (!Objects.equals(requesterAccountId, accountId)
                || !externalIamService.existsActiveAccount(accountId)) {

            throw new SubscriptionAccessDeniedException();
        }
    }
}
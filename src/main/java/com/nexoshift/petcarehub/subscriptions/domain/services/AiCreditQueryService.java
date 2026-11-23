package com.nexoshift.petcarehub.subscriptions.domain.services;

import com.nexoshift.petcarehub.subscriptions.domain.model.aggregates.AiCreditPeriod;
import com.nexoshift.petcarehub.subscriptions.domain.model.queries.GetAiCreditsByAccountIdQuery;

public interface AiCreditQueryService {

    AiCreditPeriod handle(GetAiCreditsByAccountIdQuery query);
}
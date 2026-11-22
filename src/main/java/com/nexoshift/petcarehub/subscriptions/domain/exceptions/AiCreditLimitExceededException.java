package com.nexoshift.petcarehub.subscriptions.domain.exceptions;

import com.nexoshift.petcarehub.shared.domain.exceptions.ConflictException;
import com.nexoshift.petcarehub.subscriptions.domain.model.valueobjects.AiCreditType;

public class AiCreditLimitExceededException
        extends ConflictException {

    public AiCreditLimitExceededException(AiCreditType creditType) {
        super("No available credits for " + creditType);
    }
}
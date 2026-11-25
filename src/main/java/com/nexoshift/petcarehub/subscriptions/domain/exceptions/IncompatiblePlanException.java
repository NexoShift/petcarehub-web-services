package com.nexoshift.petcarehub.subscriptions.domain.exceptions;

import com.nexoshift.petcarehub.shared.domain.exceptions.BadRequestException;

public class IncompatiblePlanException extends BadRequestException {

    public IncompatiblePlanException() {
        super("The selected plan does not match the account type");
    }
}
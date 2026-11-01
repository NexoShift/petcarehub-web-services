package com.nexoshift.petcarehub.profiles.domain.exceptions;

import com.nexoshift.petcarehub.shared.domain.exceptions.ForbiddenException;

public class ProfileOperationNotAllowedException extends ForbiddenException {

    public ProfileOperationNotAllowedException(String message) {
        super(message);
    }
}

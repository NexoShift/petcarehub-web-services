package com.nexoshift.petcarehub.clinicalexams.domain.exceptions;

import com.nexoshift.petcarehub.shared.domain.exceptions.ForbiddenException;

public class VeterinarianNotRegisteredException extends ForbiddenException {

    public VeterinarianNotRegisteredException() {
        super("The authenticated user has no veterinarian profile");
    }
}
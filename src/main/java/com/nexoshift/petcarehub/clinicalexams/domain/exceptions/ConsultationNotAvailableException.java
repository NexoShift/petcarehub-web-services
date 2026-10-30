package com.nexoshift.petcarehub.clinicalexams.domain.exceptions;

import com.nexoshift.petcarehub.shared.domain.exceptions.ConflictException;

public class ConsultationNotAvailableException extends ConflictException {

    public ConsultationNotAvailableException(Long consultationId) {
        super("Clinical consultation " + consultationId + " does not exist or is not in progress");
    }
}
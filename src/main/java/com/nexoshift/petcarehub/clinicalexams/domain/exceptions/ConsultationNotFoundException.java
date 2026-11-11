package com.nexoshift.petcarehub.clinicalexams.domain.exceptions;

import com.nexoshift.petcarehub.shared.domain.exceptions.NotFoundException;

public class ConsultationNotFoundException extends NotFoundException {

    public ConsultationNotFoundException(Long consultationId) {
        super("Clinical consultation " + consultationId + " was not found");
    }
}

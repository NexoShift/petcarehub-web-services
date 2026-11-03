package com.nexoshift.petcarehub.clinicalexams.domain.exceptions;

import com.nexoshift.petcarehub.shared.domain.exceptions.NotFoundException;

public class ClinicalFileNotFoundException extends NotFoundException {

    public ClinicalFileNotFoundException(Long fileId) {
        super("Clinical file " + fileId + " was not found");
    }
}
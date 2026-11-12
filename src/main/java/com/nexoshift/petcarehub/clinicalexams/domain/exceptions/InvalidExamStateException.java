package com.nexoshift.petcarehub.clinicalexams.domain.exceptions;

import com.nexoshift.petcarehub.shared.domain.exceptions.ConflictException;

public class InvalidExamStateException extends ConflictException {

    public InvalidExamStateException(String message) {
        super(message);
    }
}
package com.nexoshift.petcarehub.clinicalexams.domain.exceptions;

import com.nexoshift.petcarehub.shared.domain.exceptions.ForbiddenException;

public class ClinicalExamAccessDeniedException extends ForbiddenException {

    public ClinicalExamAccessDeniedException() {
        super("You are not allowed to access this clinical exam");
    }
}

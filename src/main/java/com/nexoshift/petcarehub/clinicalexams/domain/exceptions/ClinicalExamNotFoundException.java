package com.nexoshift.petcarehub.clinicalexams.domain.exceptions;

import com.nexoshift.petcarehub.shared.domain.exceptions.NotFoundException;

public class ClinicalExamNotFoundException extends NotFoundException {

    public ClinicalExamNotFoundException(Long examId) {
        super("Clinical exam " + examId + " was not found");
    }
}
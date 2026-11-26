package com.nexoshift.petcarehub.medicalhistory.domain.exceptions;

import com.nexoshift.petcarehub.shared.domain.exceptions.ForbiddenException;

public class MedicalHistoryAccessDeniedException extends ForbiddenException {

  public MedicalHistoryAccessDeniedException(String message) {
    super(message);
  }
}

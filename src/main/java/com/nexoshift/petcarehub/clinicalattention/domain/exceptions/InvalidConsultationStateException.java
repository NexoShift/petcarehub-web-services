package com.nexoshift.petcarehub.clinicalattention.domain.exceptions;

import com.nexoshift.petcarehub.shared.domain.exceptions.ConflictException;

/** The operation is not allowed in the current state of the consultation (HTTP 409). */
public class InvalidConsultationStateException extends ConflictException {

  public InvalidConsultationStateException(String message) {
    super(message);
  }
}

package com.nexoshift.petcarehub.aiassistance.domain.exceptions;

import com.nexoshift.petcarehub.shared.domain.exceptions.ConflictException;

/** An operation is not allowed in the current status of the analysis. */
public class InvalidAiAnalysisStateException extends ConflictException {

  public InvalidAiAnalysisStateException(String message) {
    super(message);
  }
}

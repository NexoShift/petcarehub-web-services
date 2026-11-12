package com.nexoshift.petcarehub.shared.domain.exceptions;

/** Base exception translated to HTTP 403 by the GlobalExceptionHandler. */
public class ForbiddenException extends RuntimeException {

  public ForbiddenException(String message) {
    super(message);
  }
}

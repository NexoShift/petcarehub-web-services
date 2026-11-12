package com.nexoshift.petcarehub.shared.domain.exceptions;

/** Base exception translated to HTTP 409 by the GlobalExceptionHandler. */
public class ConflictException extends RuntimeException {

  public ConflictException(String message) {
    super(message);
  }
}

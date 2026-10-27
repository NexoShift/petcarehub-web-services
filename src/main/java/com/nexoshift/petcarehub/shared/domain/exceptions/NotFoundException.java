package com.nexoshift.petcarehub.shared.domain.exceptions;

/** Base exception translated to HTTP 404 by the GlobalExceptionHandler. */
public class NotFoundException extends RuntimeException {

  public NotFoundException(String message) {
    super(message);
  }
}

package com.nexoshift.petcarehub.shared.domain.exceptions;

/** Base exception translated to HTTP 401 by the GlobalExceptionHandler. */
public class UnauthorizedException extends RuntimeException {

  public UnauthorizedException(String message) {
    super(message);
  }
}

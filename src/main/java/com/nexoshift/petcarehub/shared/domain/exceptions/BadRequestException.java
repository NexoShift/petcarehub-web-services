package com.nexoshift.petcarehub.shared.domain.exceptions;

/** Base exception translated to HTTP 400 by the GlobalExceptionHandler. */
public class BadRequestException extends RuntimeException {

  public BadRequestException(String message) {
    super(message);
  }
}

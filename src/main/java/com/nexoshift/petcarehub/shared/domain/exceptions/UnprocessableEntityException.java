package com.nexoshift.petcarehub.shared.domain.exceptions;

/** Base exception translated to HTTP 422 by the GlobalExceptionHandler. */
public class UnprocessableEntityException extends RuntimeException {

  public UnprocessableEntityException(String message) {
    super(message);
  }
}

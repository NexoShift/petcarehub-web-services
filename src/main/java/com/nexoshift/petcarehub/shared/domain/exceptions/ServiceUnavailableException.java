package com.nexoshift.petcarehub.shared.domain.exceptions;

/** Base exception translated to HTTP 503 by the GlobalExceptionHandler. */
public class ServiceUnavailableException extends RuntimeException {

  public ServiceUnavailableException(String message) {
    super(message);
  }

  public ServiceUnavailableException(String message, Throwable cause) {
    super(message, cause);
  }
}

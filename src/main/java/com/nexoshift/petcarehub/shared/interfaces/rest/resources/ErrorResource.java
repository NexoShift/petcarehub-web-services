package com.nexoshift.petcarehub.shared.interfaces.rest.resources;

import java.time.Instant;
import java.util.List;

/** Uniform error body returned by every endpoint. */
public record ErrorResource(
    int status, String error, String message, String path, Instant timestamp, List<String> details) {

  public static ErrorResource of(int status, String error, String message, String path) {
    return new ErrorResource(status, error, message, path, Instant.now(), List.of());
  }

  public static ErrorResource of(
      int status, String error, String message, String path, List<String> details) {
    return new ErrorResource(status, error, message, path, Instant.now(), details);
  }
}

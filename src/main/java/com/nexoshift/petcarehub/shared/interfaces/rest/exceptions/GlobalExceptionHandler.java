package com.nexoshift.petcarehub.shared.interfaces.rest.exceptions;

import com.nexoshift.petcarehub.shared.domain.exceptions.BadRequestException;
import com.nexoshift.petcarehub.shared.domain.exceptions.ConflictException;
import com.nexoshift.petcarehub.shared.domain.exceptions.ForbiddenException;
import com.nexoshift.petcarehub.shared.domain.exceptions.NotFoundException;
import com.nexoshift.petcarehub.shared.domain.exceptions.UnauthorizedException;
import com.nexoshift.petcarehub.shared.interfaces.rest.resources.ErrorResource;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

/** Translates exceptions into uniform HTTP responses (400 / 401 / 403 / 404 / 409 / 500). */
@RestControllerAdvice
public class GlobalExceptionHandler {

  private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorResource> handleValidation(
      MethodArgumentNotValidException exception, HttpServletRequest request) {
    List<String> details = exception.getBindingResult().getFieldErrors().stream()
        .map(error -> error.getField() + ": " + error.getDefaultMessage())
        .toList();
    return build(HttpStatus.BAD_REQUEST, "Invalid request data", request, details);
  }

  @ExceptionHandler({
      BadRequestException.class,
      IllegalArgumentException.class,
      HttpMessageNotReadableException.class,
      MethodArgumentTypeMismatchException.class})
  public ResponseEntity<ErrorResource> handleBadRequest(
      Exception exception, HttpServletRequest request) {
    String message = exception instanceof HttpMessageNotReadableException
        ? "Malformed request body"
        : exception.getMessage();
    return build(HttpStatus.BAD_REQUEST, message, request, List.of());
  }

  @ExceptionHandler({UnauthorizedException.class, AuthenticationException.class})
  public ResponseEntity<ErrorResource> handleUnauthorized(
      RuntimeException exception, HttpServletRequest request) {
    return build(HttpStatus.UNAUTHORIZED, exception.getMessage(), request, List.of());
  }

  @ExceptionHandler({ForbiddenException.class, AccessDeniedException.class})
  public ResponseEntity<ErrorResource> handleForbidden(
      RuntimeException exception, HttpServletRequest request) {
    String message = exception instanceof AccessDeniedException
        ? "You do not have permission to perform this action"
        : exception.getMessage();
    return build(HttpStatus.FORBIDDEN, message, request, List.of());
  }

  @ExceptionHandler(NotFoundException.class)
  public ResponseEntity<ErrorResource> handleNotFound(
      NotFoundException exception, HttpServletRequest request) {
    return build(HttpStatus.NOT_FOUND, exception.getMessage(), request, List.of());
  }

  @ExceptionHandler({ConflictException.class, IllegalStateException.class})
  public ResponseEntity<ErrorResource> handleConflict(
      RuntimeException exception, HttpServletRequest request) {
    return build(HttpStatus.CONFLICT, exception.getMessage(), request, List.of());
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResource> handleUnexpected(
      Exception exception, HttpServletRequest request) {
    LOGGER.error("Unexpected error on {}", request.getRequestURI(), exception);
    return build(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected error", request, List.of());
  }

  private ResponseEntity<ErrorResource> build(
      HttpStatus status, String message, HttpServletRequest request, List<String> details) {
    var body = ErrorResource.of(
        status.value(), status.getReasonPhrase(), message, request.getRequestURI(), details);
    return ResponseEntity.status(status).body(body);
  }
}

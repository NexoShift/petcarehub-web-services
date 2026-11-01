package com.nexoshift.petcarehub.clinicalattention.interfaces.rest.exceptions;

import com.nexoshift.petcarehub.clinicalattention.domain.exceptions.IncompleteConsultationException;
import com.nexoshift.petcarehub.shared.interfaces.rest.resources.ErrorResource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Adds the pending items to the 409 returned when a consultation cannot be completed. The other
 * errors of the context are handled by the GlobalExceptionHandler.
 */
@RestControllerAdvice(basePackages = "com.nexoshift.petcarehub.clinicalattention")
@Order(Ordered.HIGHEST_PRECEDENCE)
public class ClinicalAttentionExceptionHandler {

  @ExceptionHandler(IncompleteConsultationException.class)
  public ResponseEntity<ErrorResource> handleIncomplete(
      IncompleteConsultationException exception, HttpServletRequest request) {
    var status = HttpStatus.CONFLICT;
    var body = ErrorResource.of(status.value(), status.getReasonPhrase(), exception.getMessage(),
        request.getRequestURI(), exception.getPendingItems());
    return ResponseEntity.status(status).body(body);
  }
}

package com.nexoshift.petcarehub.iam.infrastructure.authorization.sfs.pipeline;

import tools.jackson.databind.ObjectMapper;
import com.nexoshift.petcarehub.shared.interfaces.rest.resources.ErrorResource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

/** 403 for authenticated users without the required role. */
@Component
public class ForbiddenRequestHandler implements AccessDeniedHandler {

  private final ObjectMapper objectMapper;

  public ForbiddenRequestHandler(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  @Override
  public void handle(HttpServletRequest request, HttpServletResponse response,
      AccessDeniedException accessDeniedException) throws IOException {
    var body = ErrorResource.of(HttpStatus.FORBIDDEN.value(),
        HttpStatus.FORBIDDEN.getReasonPhrase(),
        "You do not have permission to perform this action", request.getRequestURI());
    response.setStatus(HttpStatus.FORBIDDEN.value());
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    objectMapper.writeValue(response.getOutputStream(), body);
  }
}

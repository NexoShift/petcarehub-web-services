package com.nexoshift.petcarehub.clinicalattention.domain.exceptions;

import com.nexoshift.petcarehub.shared.domain.exceptions.ForbiddenException;

/** The authenticated user is not a veterinarian allowed to do this operation (HTTP 403). */
public class VeterinarianNotAuthorizedException extends ForbiddenException {

  public VeterinarianNotAuthorizedException(String message) {
    super(message);
  }
}

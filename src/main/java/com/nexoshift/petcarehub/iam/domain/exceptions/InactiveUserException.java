package com.nexoshift.petcarehub.iam.domain.exceptions;

import com.nexoshift.petcarehub.shared.domain.exceptions.ForbiddenException;

public class InactiveUserException extends ForbiddenException {

  public InactiveUserException() {
    super("The user or its account is not active");
  }
}

package com.nexoshift.petcarehub.iam.domain.exceptions;

import com.nexoshift.petcarehub.shared.domain.exceptions.ForbiddenException;

public class AccountOperationNotAllowedException extends ForbiddenException {

  public AccountOperationNotAllowedException(String reason) {
    super(reason);
  }
}

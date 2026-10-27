package com.nexoshift.petcarehub.iam.domain.exceptions;

import com.nexoshift.petcarehub.shared.domain.exceptions.ConflictException;

public class EmailAlreadyRegisteredException extends ConflictException {

  public EmailAlreadyRegisteredException(String email) {
    super("The email " + email + " is already registered");
  }
}

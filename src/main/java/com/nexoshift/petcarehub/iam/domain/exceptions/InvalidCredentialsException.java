package com.nexoshift.petcarehub.iam.domain.exceptions;

import com.nexoshift.petcarehub.shared.domain.exceptions.UnauthorizedException;

public class InvalidCredentialsException extends UnauthorizedException {

  public InvalidCredentialsException() {
    super("Invalid email or password");
  }
}

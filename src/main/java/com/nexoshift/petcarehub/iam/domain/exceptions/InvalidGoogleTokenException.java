package com.nexoshift.petcarehub.iam.domain.exceptions;

import com.nexoshift.petcarehub.shared.domain.exceptions.UnauthorizedException;

public class InvalidGoogleTokenException extends UnauthorizedException {

  public InvalidGoogleTokenException(String reason) {
    super("Invalid Google sign-in: " + reason);
  }
}

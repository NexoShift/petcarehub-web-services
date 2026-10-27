package com.nexoshift.petcarehub.iam.domain.exceptions;

import com.nexoshift.petcarehub.shared.domain.exceptions.BadRequestException;

public class InvalidPasswordResetTokenException extends BadRequestException {

  public InvalidPasswordResetTokenException() {
    super("The password reset link is invalid or has expired");
  }
}

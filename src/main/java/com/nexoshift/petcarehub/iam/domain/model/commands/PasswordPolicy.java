package com.nexoshift.petcarehub.iam.domain.model.commands;

/** Password rule shared by the commands that receive a new plain password. */
final class PasswordPolicy {

  static final int MIN_LENGTH = 8;
  static final int MAX_LENGTH = 72;

  private PasswordPolicy() {
  }

  static void validate(String password) {
    if (password == null || password.length() < MIN_LENGTH || password.length() > MAX_LENGTH) {
      throw new IllegalArgumentException(
          "Password must have between " + MIN_LENGTH + " and " + MAX_LENGTH + " characters");
    }
  }
}

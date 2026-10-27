package com.nexoshift.petcarehub.iam.domain.model.commands;

public record ResetPasswordCommand(String token, String newPassword) {

  public ResetPasswordCommand {
    if (token == null || token.isBlank()) {
      throw new IllegalArgumentException("Reset token is required");
    }
    PasswordPolicy.validate(newPassword);
  }
}

package com.nexoshift.petcarehub.iam.domain.model.commands;

public record ChangePasswordCommand(Long userId, String currentPassword, String newPassword) {

  public ChangePasswordCommand {
    if (userId == null || userId <= 0) {
      throw new IllegalArgumentException("User id is required");
    }
    if (currentPassword == null || currentPassword.isBlank()) {
      throw new IllegalArgumentException("Current password is required");
    }
    PasswordPolicy.validate(newPassword);
  }
}

package com.nexoshift.petcarehub.iam.domain.model.commands;

public record RequestPasswordRecoveryCommand(String email) {

  public RequestPasswordRecoveryCommand {
    if (email == null || email.isBlank()) {
      throw new IllegalArgumentException("Email is required");
    }
  }
}

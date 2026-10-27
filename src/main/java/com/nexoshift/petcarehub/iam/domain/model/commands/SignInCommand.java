package com.nexoshift.petcarehub.iam.domain.model.commands;

public record SignInCommand(String email, String password) {

  public SignInCommand {
    if (email == null || email.isBlank() || password == null || password.isBlank()) {
      throw new IllegalArgumentException("Email and password are required");
    }
  }
}

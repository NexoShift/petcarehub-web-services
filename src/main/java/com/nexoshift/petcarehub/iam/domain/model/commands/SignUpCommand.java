package com.nexoshift.petcarehub.iam.domain.model.commands;

import com.nexoshift.petcarehub.iam.domain.model.valueobjects.AccountType;

public record SignUpCommand(String email, String password, AccountType accountType) {

  public SignUpCommand {
    PasswordPolicy.validate(password);
    if (accountType == null) {
      throw new IllegalArgumentException("Account type is required");
    }
  }
}

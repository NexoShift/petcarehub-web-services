package com.nexoshift.petcarehub.iam.domain.model.commands;

/** Used by Profiles (through the IAM facade) to create a veterinarian user in a clinic account. */
public record CreateMemberUserCommand(Long accountId, String email, String password) {

  public CreateMemberUserCommand {
    if (accountId == null || accountId <= 0) {
      throw new IllegalArgumentException("Account id is required");
    }
    PasswordPolicy.validate(password);
  }
}

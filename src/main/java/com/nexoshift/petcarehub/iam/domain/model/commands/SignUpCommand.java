package com.nexoshift.petcarehub.iam.domain.model.commands;

import com.nexoshift.petcarehub.iam.domain.model.valueobjects.AccountType;
import com.nexoshift.petcarehub.iam.domain.model.valueobjects.HolderProfile;

public record SignUpCommand(String email, String password, AccountType accountType,
                            HolderProfile profile) {
  public SignUpCommand {
    PasswordPolicy.validate(password);
    if (accountType == null) throw new IllegalArgumentException("Account type is required");
    if (profile == null || profile.phoneNumber() == null)
      throw new IllegalArgumentException("Phone number is required");
    if (accountType == AccountType.CLINIC
            && (profile.clinicName() == null || profile.clinicName().isBlank()))
      throw new IllegalArgumentException("Clinic name is required for clinic accounts");
    if (accountType == AccountType.PET_OWNER) {
      profile = new HolderProfile(profile.firstName(), profile.lastName(),
              profile.phoneNumber(), null, null);
    }
  }
}

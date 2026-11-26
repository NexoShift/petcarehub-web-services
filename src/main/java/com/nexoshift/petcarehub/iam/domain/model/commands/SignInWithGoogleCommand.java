package com.nexoshift.petcarehub.iam.domain.model.commands;

import com.nexoshift.petcarehub.iam.domain.model.valueobjects.AccountType;

/**
 * Sign in with the ID token that Google gave the frontend.
 *
 * @param idToken the Google ID token (a JWT signed by Google)
 * @param accountType only needed the first time, to create the account (PET_OWNER or CLINIC)
 */
public record SignInWithGoogleCommand(String idToken, AccountType accountType) {

  public SignInWithGoogleCommand {
    if (idToken == null || idToken.isBlank()) {
      throw new IllegalArgumentException("Google ID token is required");
    }
  }
}

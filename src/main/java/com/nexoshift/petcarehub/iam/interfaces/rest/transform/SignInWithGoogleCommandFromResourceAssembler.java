package com.nexoshift.petcarehub.iam.interfaces.rest.transform;

import com.nexoshift.petcarehub.iam.domain.model.commands.SignInWithGoogleCommand;
import com.nexoshift.petcarehub.iam.domain.model.valueobjects.AccountType;
import com.nexoshift.petcarehub.iam.interfaces.rest.resources.GoogleSignInResource;

public final class SignInWithGoogleCommandFromResourceAssembler {

  private SignInWithGoogleCommandFromResourceAssembler() {
  }

  public static SignInWithGoogleCommand toCommandFromResource(GoogleSignInResource resource) {
    var accountType = resource.accountType() == null || resource.accountType().isBlank()
        ? null
        : AccountType.valueOf(resource.accountType().trim().toUpperCase());
    return new SignInWithGoogleCommand(resource.idToken(), accountType);
  }
}

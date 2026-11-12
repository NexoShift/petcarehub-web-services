package com.nexoshift.petcarehub.iam.interfaces.rest.transform;

import com.nexoshift.petcarehub.iam.domain.model.commands.SignInCommand;
import com.nexoshift.petcarehub.iam.interfaces.rest.resources.SignInResource;

public final class SignInCommandFromResourceAssembler {

  private SignInCommandFromResourceAssembler() {
  }

  public static SignInCommand toCommandFromResource(SignInResource resource) {
    return new SignInCommand(resource.email(), resource.password());
  }
}

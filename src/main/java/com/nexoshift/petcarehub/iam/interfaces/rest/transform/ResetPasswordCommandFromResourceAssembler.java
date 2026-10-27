package com.nexoshift.petcarehub.iam.interfaces.rest.transform;

import com.nexoshift.petcarehub.iam.domain.model.commands.ResetPasswordCommand;
import com.nexoshift.petcarehub.iam.interfaces.rest.resources.PasswordResetResource;

public final class ResetPasswordCommandFromResourceAssembler {

  private ResetPasswordCommandFromResourceAssembler() {
  }

  public static ResetPasswordCommand toCommandFromResource(PasswordResetResource resource) {
    return new ResetPasswordCommand(resource.token(), resource.newPassword());
  }
}

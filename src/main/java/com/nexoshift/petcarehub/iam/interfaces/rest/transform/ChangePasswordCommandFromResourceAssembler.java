package com.nexoshift.petcarehub.iam.interfaces.rest.transform;

import com.nexoshift.petcarehub.iam.domain.model.commands.ChangePasswordCommand;
import com.nexoshift.petcarehub.iam.interfaces.rest.resources.ChangePasswordResource;

public final class ChangePasswordCommandFromResourceAssembler {

  private ChangePasswordCommandFromResourceAssembler() {
  }

  public static ChangePasswordCommand toCommandFromResource(
      Long userId, ChangePasswordResource resource) {
    return new ChangePasswordCommand(userId, resource.currentPassword(), resource.newPassword());
  }
}

package com.nexoshift.petcarehub.iam.domain.exceptions;

import com.nexoshift.petcarehub.shared.domain.exceptions.NotFoundException;

public class RoleNotFoundException extends NotFoundException {

  public RoleNotFoundException(String role) {
    super("Role " + role + " was not found");
  }
}

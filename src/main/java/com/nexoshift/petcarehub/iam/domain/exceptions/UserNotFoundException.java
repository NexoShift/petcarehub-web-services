package com.nexoshift.petcarehub.iam.domain.exceptions;

import com.nexoshift.petcarehub.shared.domain.exceptions.NotFoundException;

public class UserNotFoundException extends NotFoundException {

  public UserNotFoundException(Long userId) {
    super("User " + userId + " was not found");
  }
}

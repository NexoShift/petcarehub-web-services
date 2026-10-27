package com.nexoshift.petcarehub.iam.domain.exceptions;

import com.nexoshift.petcarehub.shared.domain.exceptions.NotFoundException;

public class AccountNotFoundException extends NotFoundException {

  public AccountNotFoundException(Long accountId) {
    super("Account " + accountId + " was not found");
  }
}

package com.nexoshift.petcarehub.iam.domain.exceptions;

import com.nexoshift.petcarehub.shared.domain.exceptions.UnprocessableEntityException;

/** First sign-in with Google: the frontend must ask whether it is a pet owner or a clinic. */
public class AccountTypeRequiredException extends UnprocessableEntityException {

  public AccountTypeRequiredException() {
    super("ACCOUNT_TYPE_REQUIRED: there is no account with this Google email yet; "
        + "send accountType (PET_OWNER or CLINIC) to create it");
  }
}

package com.nexoshift.petcarehub.iam.domain.model.valueobjects;

/** Public reference to an Account, used by other bounded contexts. */
public record AccountId(Long accountId) {

  public AccountId {
    if (accountId == null || accountId <= 0) {
      throw new IllegalArgumentException("Account id must be greater than zero");
    }
  }
}

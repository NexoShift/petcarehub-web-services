package com.nexoshift.petcarehub.profiles.domain.model.valueobjects;

import jakarta.persistence.Embeddable;

@Embeddable
public record AccountId(Long accountId) {

  public AccountId {
    if (accountId == null || accountId <= 0) {
      throw new IllegalArgumentException("Account ID must be greater than zero");
    }
  }
}

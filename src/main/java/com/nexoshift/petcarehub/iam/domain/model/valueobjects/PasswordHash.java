package com.nexoshift.petcarehub.iam.domain.model.valueobjects;

import jakarta.persistence.Embeddable;

/** Hashed password. The domain never handles plain text passwords. */
@Embeddable
public record PasswordHash(String value) {

  public PasswordHash {
    if (value == null || value.isBlank()) {
      throw new IllegalArgumentException("Password hash is required");
    }
  }

  @Override
  public String toString() {
    return "PasswordHash[PROTECTED]";
  }
}

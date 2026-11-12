package com.nexoshift.petcarehub.iam.domain.model.valueobjects;

import jakarta.persistence.Embeddable;

/** Public reference to a User, used by other bounded contexts. */
@Embeddable
public record UserId(Long userId) {

  public UserId {
    if (userId == null || userId <= 0) {
      throw new IllegalArgumentException("User id must be greater than zero");
    }
  }
}

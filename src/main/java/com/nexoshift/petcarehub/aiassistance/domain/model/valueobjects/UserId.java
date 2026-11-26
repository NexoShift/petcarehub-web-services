package com.nexoshift.petcarehub.aiassistance.domain.model.valueobjects;

import jakarta.persistence.Embeddable;

/** IAM user id of the pet owner who asks. */
@Embeddable
public record UserId(Long userId) {

  public UserId {
    if (userId == null || userId <= 0) {
      throw new IllegalArgumentException("User id must be greater than zero");
    }
  }
}

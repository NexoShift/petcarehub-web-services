package com.nexoshift.petcarehub.profiles.domain.model.valueobjects;

import jakarta.persistence.Embeddable;

@Embeddable
public record UserId(Long userId) {

  public UserId {
    if (userId == null || userId <= 0) {
      throw new IllegalArgumentException("User ID must be greater than zero");
    }
  }
}

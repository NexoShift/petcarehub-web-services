package com.nexoshift.petcarehub.profiles.domain.model.valueobjects;

import jakarta.persistence.Embeddable;

@Embeddable
public record OwnerId(Long ownerId) {

  public OwnerId {
    if (ownerId == null || ownerId <= 0) {
      throw new IllegalArgumentException("Owner ID must be greater than zero");
    }
  }
}

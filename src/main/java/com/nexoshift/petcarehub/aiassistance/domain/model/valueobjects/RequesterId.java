package com.nexoshift.petcarehub.aiassistance.domain.model.valueobjects;

import jakarta.persistence.Embeddable;

/** IAM user id of whoever uploaded the image (veterinarian or pet owner). */
@Embeddable
public record RequesterId(Long requesterId) {

  public RequesterId {
    if (requesterId == null || requesterId <= 0) {
      throw new IllegalArgumentException("Requester id must be greater than zero");
    }
  }
}

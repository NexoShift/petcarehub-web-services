package com.nexoshift.petcarehub.aiassistance.domain.model.valueobjects;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

/** A background the veterinarian chose to send to the LLM: what it is and which record. */
@Embeddable
public record SelectedBackground(
    @Enumerated(EnumType.STRING) @Column(name = "background_type", length = 30)
    BackgroundType type,
    Long referenceId) {

  public SelectedBackground {
    if (type == null) {
      throw new IllegalArgumentException("Background type is required");
    }
    if (referenceId == null || referenceId <= 0) {
      throw new IllegalArgumentException("Background reference id must be greater than zero");
    }
  }
}

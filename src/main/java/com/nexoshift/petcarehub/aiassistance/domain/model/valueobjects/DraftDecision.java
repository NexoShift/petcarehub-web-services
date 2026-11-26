package com.nexoshift.petcarehub.aiassistance.domain.model.valueobjects;

/** What the veterinarian did with a SOAP draft. */
public enum DraftDecision {
  PENDING,
  ACCEPTED,
  EDITED,
  DISCARDED;

  public boolean isConfirmed() {
    return this == ACCEPTED || this == EDITED;
  }
}

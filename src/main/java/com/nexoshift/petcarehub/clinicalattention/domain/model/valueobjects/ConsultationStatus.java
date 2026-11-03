package com.nexoshift.petcarehub.clinicalattention.domain.model.valueobjects;

/** Life cycle of a clinical consultation. */
public enum ConsultationStatus {
  CREATED,
  IN_PROGRESS,
  COMPLETED,
  CANCELLED;

  /** A consultation can be cancelled until it is completed. */
  public boolean allowsCancellation() {
    return this == CREATED || this == IN_PROGRESS;
  }
}

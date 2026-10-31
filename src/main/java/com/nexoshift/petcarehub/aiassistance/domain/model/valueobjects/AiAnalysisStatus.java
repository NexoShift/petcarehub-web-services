package com.nexoshift.petcarehub.aiassistance.domain.model.valueobjects;

/** Lifecycle of an AI request: shared by clinical assistances and vision analyses. */
public enum AiAnalysisStatus {
  PENDING,
  PROCESSING,
  COMPLETED,
  FAILED
}

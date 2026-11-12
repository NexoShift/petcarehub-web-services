package com.nexoshift.petcarehub.clinicalattention.domain.model.valueobjects;

/** A diagnosis is SUPERSEDED when a reevaluation replaces it; its content never changes. */
public enum DiagnosisStatus {
  CURRENT,
  SUPERSEDED
}

package com.nexoshift.petcarehub.clinicalattention.domain.model.entities;

import com.nexoshift.petcarehub.clinicalattention.domain.model.valueobjects.BodySystem;
import com.nexoshift.petcarehub.clinicalattention.domain.model.valueobjects.ClinicalFieldLimits;
import com.nexoshift.petcarehub.clinicalattention.domain.model.valueobjects.FindingSeverity;
import com.nexoshift.petcarehub.shared.domain.model.entities.AuditableModel;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

/** Observation identified by the veterinarian during the examination. Findings are append-only. */
@Entity
public class ClinicalFinding extends AuditableModel {

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 30)
  private BodySystem bodySystem;

  @Column(nullable = false, length = ClinicalFieldLimits.FINDING_DESCRIPTION)
  private String description;

  @Enumerated(EnumType.STRING)
  @Column(length = 20)
  private FindingSeverity severity;

  protected ClinicalFinding() {
  }

  public ClinicalFinding(BodySystem bodySystem, String description, FindingSeverity severity) {
    if (bodySystem == null) {
      throw new IllegalArgumentException("Body system is required");
    }
    if (description == null || description.isBlank()) {
      throw new IllegalArgumentException("Finding description is required");
    }
    this.bodySystem = bodySystem;
    this.description = description;
    this.severity = severity;
  }

  public BodySystem getBodySystem() {
    return bodySystem;
  }

  public String getDescription() {
    return description;
  }

  public FindingSeverity getSeverity() {
    return severity;
  }
}

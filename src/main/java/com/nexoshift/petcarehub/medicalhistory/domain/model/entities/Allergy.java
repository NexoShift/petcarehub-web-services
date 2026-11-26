package com.nexoshift.petcarehub.medicalhistory.domain.model.entities;

import com.nexoshift.petcarehub.medicalhistory.domain.model.valueobjects.AllergySeverity;
import com.nexoshift.petcarehub.medicalhistory.domain.model.valueobjects.MedicalHistoryFieldLimits;
import com.nexoshift.petcarehub.medicalhistory.domain.model.valueobjects.ReporterType;
import com.nexoshift.petcarehub.shared.domain.model.entities.AuditableModel;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

import java.time.Instant;

/** Known allergy of the pet (Alergia). Records are never changed or deleted. */
@Entity
public class Allergy extends AuditableModel {

  @Column(nullable = false, length = MedicalHistoryFieldLimits.ALLERGEN)
  private String allergen;

  @Column(length = MedicalHistoryFieldLimits.REACTION)
  private String reaction;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private AllergySeverity severity;

  @Column(length = MedicalHistoryFieldLimits.NOTES)
  private String notes;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private ReporterType reportedBy;

  @Column(nullable = false)
  private Long reporterUserId;

  @Column(nullable = false)
  private Instant recordedAt;

  protected Allergy() {
  }

  public Allergy(String allergen, String reaction, AllergySeverity severity, String notes,
      ReporterType reportedBy, Long reporterUserId) {
    if (allergen == null || allergen.isBlank()) {
      throw new IllegalArgumentException("Allergen is required");
    }
    if (severity == null) {
      throw new IllegalArgumentException("Allergy severity is required");
    }
    if (reportedBy == null || reporterUserId == null) {
      throw new IllegalArgumentException("Reporter is required");
    }
    this.allergen = allergen;
    this.reaction = reaction;
    this.severity = severity;
    this.notes = notes;
    this.reportedBy = reportedBy;
    this.reporterUserId = reporterUserId;
    this.recordedAt = Instant.now();
  }

  public String getAllergen() {
    return allergen;
  }

  public String getReaction() {
    return reaction;
  }

  public AllergySeverity getSeverity() {
    return severity;
  }

  public String getNotes() {
    return notes;
  }

  public ReporterType getReportedBy() {
    return reportedBy;
  }

  public Long getReporterUserId() {
    return reporterUserId;
  }

  public Instant getRecordedAt() {
    return recordedAt;
  }
}

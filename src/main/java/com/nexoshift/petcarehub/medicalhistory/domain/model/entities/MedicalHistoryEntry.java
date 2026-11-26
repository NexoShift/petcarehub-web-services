package com.nexoshift.petcarehub.medicalhistory.domain.model.entities;

import com.nexoshift.petcarehub.medicalhistory.domain.model.valueobjects.HistoryEntryType;
import com.nexoshift.petcarehub.medicalhistory.domain.model.valueobjects.MedicalHistoryFieldLimits;
import com.nexoshift.petcarehub.shared.domain.model.entities.AuditableModel;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

import java.time.Instant;

/**
 * One mark in the longitudinal record. It keeps only a reference ({@code referenceId}) to the
 * consultation or exam of the other context plus a short summary, never a copy of the record.
 */
@Entity
public class MedicalHistoryEntry extends AuditableModel {

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 30)
  private HistoryEntryType type;

  @Column(nullable = false)
  private Long referenceId;

  private Long consultationId;

  @Column(nullable = false)
  private Long clinicId;

  private Long veterinarianId;

  @Column(length = MedicalHistoryFieldLimits.EXAM_TYPE)
  private String examType;

  @Column(length = MedicalHistoryFieldLimits.ENTRY_SUMMARY)
  private String summary;

  @Column(nullable = false)
  private Instant occurredAt;

  protected MedicalHistoryEntry() {
  }

  public MedicalHistoryEntry(HistoryEntryType type, Long referenceId, Long consultationId,
      Long clinicId, Long veterinarianId, String examType, String summary, Instant occurredAt) {
    if (type == null || referenceId == null || clinicId == null || occurredAt == null) {
      throw new IllegalArgumentException("Type, reference, clinic and date are required");
    }
    this.type = type;
    this.referenceId = referenceId;
    this.consultationId = consultationId;
    this.clinicId = clinicId;
    this.veterinarianId = veterinarianId;
    this.examType = examType;
    this.summary = truncate(summary);
    this.occurredAt = occurredAt;
  }

  private static String truncate(String text) {
    if (text == null || text.length() <= MedicalHistoryFieldLimits.ENTRY_SUMMARY) {
      return text;
    }
    return text.substring(0, MedicalHistoryFieldLimits.ENTRY_SUMMARY);
  }

  public boolean refersTo(HistoryEntryType type, Long referenceId) {
    return this.type == type && this.referenceId.equals(referenceId);
  }

  public HistoryEntryType getType() {
    return type;
  }

  public Long getReferenceId() {
    return referenceId;
  }

  public Long getConsultationId() {
    return consultationId;
  }

  public Long getClinicId() {
    return clinicId;
  }

  public Long getVeterinarianId() {
    return veterinarianId;
  }

  public String getExamType() {
    return examType;
  }

  public String getSummary() {
    return summary;
  }

  public Instant getOccurredAt() {
    return occurredAt;
  }
}

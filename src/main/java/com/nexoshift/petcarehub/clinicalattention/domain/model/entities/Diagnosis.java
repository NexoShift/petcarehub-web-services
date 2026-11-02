package com.nexoshift.petcarehub.clinicalattention.domain.model.entities;

import com.nexoshift.petcarehub.clinicalattention.domain.exceptions.InvalidConsultationStateException;
import com.nexoshift.petcarehub.clinicalattention.domain.model.valueobjects.ClinicalFieldLimits;
import com.nexoshift.petcarehub.clinicalattention.domain.model.valueobjects.DiagnosisStatus;
import com.nexoshift.petcarehub.clinicalattention.domain.model.valueobjects.DiagnosisType;
import com.nexoshift.petcarehub.clinicalattention.domain.model.valueobjects.VeterinarianId;
import com.nexoshift.petcarehub.shared.domain.model.entities.AuditableModel;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OrderColumn;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

/**
 * Clinical conclusion established by the veterinarian. Its content is never modified: a
 * reevaluation is a new Diagnosis that points to the previous one, which is only marked as
 * SUPERSEDED (CON-01).
 */
@Entity
public class Diagnosis extends AuditableModel {

  @Column(nullable = false, length = ClinicalFieldLimits.DIAGNOSIS_DESCRIPTION)
  private String description;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private DiagnosisType type;

  @Column(name = "is_primary", nullable = false)
  private boolean primary;

  @ElementCollection(fetch = FetchType.EAGER)
  @CollectionTable(name = "diagnosis_differential_diagnoses",
      joinColumns = @JoinColumn(name = "diagnosis_id"))
  @Column(name = "differential_diagnosis", length = ClinicalFieldLimits.DIFFERENTIAL_DIAGNOSIS)
  @OrderColumn(name = "differential_order")
  @Fetch(FetchMode.SELECT)
  private List<String> differentialDiagnoses = new ArrayList<>();

  @Column(length = ClinicalFieldLimits.RATIONALE)
  private String rationale;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private DiagnosisStatus status;

  private Long previousDiagnosisId;

  @Embedded
  @AttributeOverride(name = "veterinarianId",
      column = @Column(name = "diagnosed_by_veterinarian_id", nullable = false))
  private VeterinarianId diagnosedBy;

  @Column(nullable = false)
  private Instant diagnosedAt;

  protected Diagnosis() {
  }

  public Diagnosis(String description, DiagnosisType type, boolean primary,
      List<String> differentialDiagnoses, String rationale, VeterinarianId diagnosedBy,
      Long previousDiagnosisId) {
    if (description == null || description.isBlank()) {
      throw new IllegalArgumentException("Diagnosis description is required");
    }
    if (type == null) {
      throw new IllegalArgumentException("Diagnosis type is required");
    }
    if (diagnosedBy == null) {
      throw new IllegalArgumentException("Veterinarian is required");
    }
    this.description = description;
    this.type = type;
    this.primary = primary;
    if (differentialDiagnoses != null) {
      this.differentialDiagnoses.addAll(differentialDiagnoses);
    }
    this.rationale = rationale;
    this.status = DiagnosisStatus.CURRENT;
    this.previousDiagnosisId = previousDiagnosisId;
    this.diagnosedBy = diagnosedBy;
    this.diagnosedAt = Instant.now();
  }

  /** Marks this diagnosis as replaced by a reevaluation. Nothing else about it changes. */
  public void markAsSuperseded() {
    if (status == DiagnosisStatus.SUPERSEDED) {
      throw new InvalidConsultationStateException("The diagnosis was already reevaluated");
    }
    this.status = DiagnosisStatus.SUPERSEDED;
  }

  public boolean isCurrent() {
    return status == DiagnosisStatus.CURRENT;
  }

  public boolean isReevaluation() {
    return previousDiagnosisId != null;
  }

  public String getDescription() {
    return description;
  }

  public DiagnosisType getType() {
    return type;
  }

  public boolean isPrimary() {
    return primary;
  }

  public List<String> getDifferentialDiagnoses() {
    return List.copyOf(differentialDiagnoses);
  }

  public String getRationale() {
    return rationale;
  }

  public DiagnosisStatus getStatus() {
    return status;
  }

  public Long getPreviousDiagnosisId() {
    return previousDiagnosisId;
  }

  public VeterinarianId getDiagnosedBy() {
    return diagnosedBy;
  }

  public Instant getDiagnosedAt() {
    return diagnosedAt;
  }
}

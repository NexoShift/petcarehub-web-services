package com.nexoshift.petcarehub.clinicalattention.domain.model.entities;

import com.nexoshift.petcarehub.clinicalattention.domain.model.valueobjects.BodyConditionScore;
import com.nexoshift.petcarehub.clinicalattention.domain.model.valueobjects.ClinicalFieldLimits;
import com.nexoshift.petcarehub.clinicalattention.domain.model.valueobjects.HydrationStatus;
import com.nexoshift.petcarehub.clinicalattention.domain.model.valueobjects.VitalSigns;
import com.nexoshift.petcarehub.shared.domain.model.entities.AuditableModel;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import java.math.BigDecimal;
import java.time.Instant;

/**
 * One measurement of the pet during the consultation. Every measurement is a new record: earlier
 * ones are never overwritten (CON-01).
 */
@Entity
public class PhysiologicalAssessment extends AuditableModel {

  @Embedded
  private VitalSigns vitalSigns;

  @Embedded
  private BodyConditionScore bodyConditionScore;

  @Enumerated(EnumType.STRING)
  @Column(length = 30)
  private HydrationStatus hydrationStatus;

  @Column(length = ClinicalFieldLimits.MUCOUS_MEMBRANES)
  private String mucousMembranes;

  @Column(precision = 3, scale = 1)
  private BigDecimal capillaryRefillTimeSeconds;

  @Column(length = ClinicalFieldLimits.ASSESSMENT_OBSERVATIONS)
  private String observations;

  @Column(nullable = false)
  private Instant recordedAt;

  protected PhysiologicalAssessment() {
  }

  public PhysiologicalAssessment(VitalSigns vitalSigns, BodyConditionScore bodyConditionScore,
      HydrationStatus hydrationStatus, String mucousMembranes,
      BigDecimal capillaryRefillTimeSeconds, String observations) {
    if (vitalSigns == null) {
      throw new IllegalArgumentException("Vital signs are required");
    }
    if (capillaryRefillTimeSeconds != null && capillaryRefillTimeSeconds.signum() <= 0) {
      throw new IllegalArgumentException("Capillary refill time (seconds) must be greater than 0");
    }
    this.vitalSigns = vitalSigns;
    this.bodyConditionScore = bodyConditionScore;
    this.hydrationStatus = hydrationStatus;
    this.mucousMembranes = mucousMembranes;
    this.capillaryRefillTimeSeconds = capillaryRefillTimeSeconds;
    this.observations = observations;
    this.recordedAt = Instant.now();
  }

  public VitalSigns getVitalSigns() {
    return vitalSigns;
  }

  public BodyConditionScore getBodyConditionScore() {
    return bodyConditionScore;
  }

  public HydrationStatus getHydrationStatus() {
    return hydrationStatus;
  }

  public String getMucousMembranes() {
    return mucousMembranes;
  }

  public BigDecimal getCapillaryRefillTimeSeconds() {
    return capillaryRefillTimeSeconds;
  }

  public String getObservations() {
    return observations;
  }

  public Instant getRecordedAt() {
    return recordedAt;
  }
}

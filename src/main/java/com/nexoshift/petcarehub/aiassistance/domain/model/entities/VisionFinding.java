package com.nexoshift.petcarehub.aiassistance.domain.model.entities;

import com.nexoshift.petcarehub.aiassistance.domain.model.aggregates.VisionAnalysis;
import com.nexoshift.petcarehub.aiassistance.domain.model.valueobjects.BoundingBox;
import com.nexoshift.petcarehub.aiassistance.domain.model.valueobjects.ConfidenceScore;
import com.nexoshift.petcarehub.shared.domain.model.entities.AuditableModel;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

/** A lesion (skin, with a box) or a probable disease (radiograph, without a box). */
@Entity
public class VisionFinding extends AuditableModel {

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "vision_analysis_id")
  private VisionAnalysis visionAnalysis;

  @Column(nullable = false, length = 60)
  private String label;

  @Embedded
  private ConfidenceScore confidence;

  @Embedded
  private BoundingBox boundingBox;

  protected VisionFinding() {
  }

  /** Public because the aggregate lives in another package; only the aggregate calls it. */
  public VisionFinding(VisionAnalysis visionAnalysis, String label, ConfidenceScore confidence,
      BoundingBox boundingBox) {
    if (label == null || label.isBlank()) {
      throw new IllegalArgumentException("Finding label is required");
    }
    this.visionAnalysis = visionAnalysis;
    this.label = label.trim();
    this.confidence = confidence;
    this.boundingBox = boundingBox;
  }

  public boolean hasLocation() {
    return boundingBox != null && boundingBox.left() != null;
  }

  public boolean isProbable() {
    return confidence.isProbable();
  }

  public String getLabel() {
    return label;
  }

  public ConfidenceScore getConfidence() {
    return confidence;
  }

  public BoundingBox getBoundingBox() {
    return hasLocation() ? boundingBox : null;
  }
}

package com.nexoshift.petcarehub.aiassistance.domain.model.valueobjects;

import jakarta.persistence.Embeddable;

/** Model confidence between 0 and 1. A finding is probable from 0.50. */
@Embeddable
public record ConfidenceScore(Double confidence) {

  public static final double PROBABLE_THRESHOLD = 0.50;

  public ConfidenceScore {
    if (confidence == null || confidence.isNaN() || confidence < 0 || confidence > 1) {
      throw new IllegalArgumentException("Confidence must be between 0 and 1");
    }
  }

  public boolean isAtLeast(double threshold) {
    return confidence >= threshold;
  }

  public boolean isProbable() {
    return isAtLeast(PROBABLE_THRESHOLD);
  }

  public int asPercentage() {
    return (int) Math.round(confidence * 100);
  }
}

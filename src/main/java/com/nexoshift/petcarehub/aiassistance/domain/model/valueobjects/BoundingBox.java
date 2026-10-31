package com.nexoshift.petcarehub.aiassistance.domain.model.valueobjects;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

/** Normalized box (0..1) from the top-left corner, independent of the displayed size. */
@Embeddable
public record BoundingBox(
    @Column(name = "box_left") Double left,
    @Column(name = "box_top") Double top,
    @Column(name = "box_width") Double width,
    @Column(name = "box_height") Double height) {

  public BoundingBox {
    if (left == null || top == null || width == null || height == null) {
      throw new IllegalArgumentException("Bounding box values are required");
    }
    left = clamp(left);
    top = clamp(top);
    width = Math.min(clamp(width), 1 - left);
    height = Math.min(clamp(height), 1 - top);
  }

  private static double clamp(double value) {
    return Math.max(0, Math.min(1, value));
  }
}

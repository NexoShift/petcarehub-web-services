package com.nexoshift.petcarehub.aiassistance.application.internal.outboundservices.vision;

import java.util.List;

/** What the model detected, already with normalized boxes. Independent of Roboflow. */
public record VisionModelResult(String modelId, List<Detection> detections) {

  public VisionModelResult {
    detections = detections == null ? List.of() : List.copyOf(detections);
  }

  /** The box values are null for radiographs (classification has no location). */
  public record Detection(String label, double confidence,
      Double left, Double top, Double width, Double height) {

    public boolean hasLocation() {
      return left != null && top != null && width != null && height != null;
    }
  }
}

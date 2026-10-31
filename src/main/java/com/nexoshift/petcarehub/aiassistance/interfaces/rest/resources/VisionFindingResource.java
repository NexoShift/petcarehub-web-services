package com.nexoshift.petcarehub.aiassistance.interfaces.rest.resources;

/**
 * Box values are normalized (0..1) from the top-left corner and null when hasLocation is
 * false (radiographs). probable = confidence of 50 % or more.
 */
public record VisionFindingResource(Long id, String label, int confidencePercentage,
    boolean probable, boolean hasLocation, Double left, Double top, Double width,
    Double height) {
}

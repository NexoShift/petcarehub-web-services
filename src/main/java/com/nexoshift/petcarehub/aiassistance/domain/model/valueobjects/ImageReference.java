package com.nexoshift.petcarehub.aiassistance.domain.model.valueobjects;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

/**
 * Where the image is stored. Only a provider-independent key such as
 * "vision/2026/11/5b1e...jpg": the same key works with local disk and with S3.
 */
@Embeddable
public record ImageReference(
    @Column(name = "image_key", length = 200) String storageKey,
    @Column(name = "image_content_type", length = 50) String contentType,
    @Column(name = "image_size_bytes") Long sizeBytes) {

  public ImageReference {
    if (storageKey == null || storageKey.isBlank()) {
      throw new IllegalArgumentException("Storage key is required");
    }
  }
}

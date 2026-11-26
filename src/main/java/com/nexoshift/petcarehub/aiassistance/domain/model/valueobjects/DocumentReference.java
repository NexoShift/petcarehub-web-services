package com.nexoshift.petcarehub.aiassistance.domain.model.valueobjects;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

/** Where the confirmed report PDF is stored. Only the storage key, never a path or URL. */
@Embeddable
public record DocumentReference(
    @Column(name = "report_key", length = 200) String storageKey,
    @Column(name = "report_content_type", length = 50) String contentType,
    @Column(name = "report_size_bytes") Long sizeBytes) {

  public DocumentReference {
    if (storageKey == null || storageKey.isBlank()) {
      throw new IllegalArgumentException("Storage key is required");
    }
  }
}

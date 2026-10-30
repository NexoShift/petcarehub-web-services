package com.nexoshift.petcarehub.clinicalexams.domain.model.valueobjects;

import jakarta.persistence.Embeddable;

/** Where the binary content of a clinical file is stored (S3 bucket or local folder). */
@Embeddable
public record StorageLocation(String bucket, String key) {

    public StorageLocation {
        if (bucket == null || bucket.isBlank() || key == null || key.isBlank()) {
            throw new IllegalArgumentException("Storage bucket and key are required");
        }
    }
}
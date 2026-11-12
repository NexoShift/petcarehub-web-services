package com.nexoshift.petcarehub.clinicalexams.interfaces.rest.resources;

import java.time.Instant;

public record ClinicalFileResource(
        Long id, String fileName, String type, String contentType, Long sizeBytes,
        Instant uploadedAt) {
}

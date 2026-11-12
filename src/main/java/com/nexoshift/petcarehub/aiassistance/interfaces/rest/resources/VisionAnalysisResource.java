package com.nexoshift.petcarehub.aiassistance.interfaces.rest.resources;

import java.time.Instant;
import java.util.List;

/** imageUrl points to the backend endpoint, never to the disk or to S3. */
public record VisionAnalysisResource(Long id, Long petId, Long consultationId, String imageType,
    String status, String modelId, String failureReason, String requestedByRole,
    String imageUrl, List<VisionFindingResource> findings, Instant createdAt) {
}

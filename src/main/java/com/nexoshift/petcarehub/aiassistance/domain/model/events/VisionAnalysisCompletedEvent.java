package com.nexoshift.petcarehub.aiassistance.domain.model.events;

public record VisionAnalysisCompletedEvent(Long visionAnalysisId, Long petId, int findings) {
}

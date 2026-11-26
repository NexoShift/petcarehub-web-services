package com.nexoshift.petcarehub.aiassistance.interfaces.rest.resources;

import java.time.Instant;
import java.util.List;

public record ClinicalAssistanceResource(Long id, Long consultationId, Long petId,
    Long veterinarianId, String currentReason, String status, String failureReason,
    List<SelectedBackgroundResource> backgrounds, List<SoapDraftResource> drafts,
    Instant createdAt) {
}

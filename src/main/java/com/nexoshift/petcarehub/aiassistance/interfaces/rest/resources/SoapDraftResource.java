package com.nexoshift.petcarehub.aiassistance.interfaces.rest.resources;

import java.time.Instant;
import java.util.List;

/** reportUrl is null until the draft is accepted or edited. */
public record SoapDraftResource(Long id, String subjective, String objective,
    String assessment, String plan, List<String> warnings, String model, String decision,
    String editedText, String reportUrl, Instant createdAt) {
}

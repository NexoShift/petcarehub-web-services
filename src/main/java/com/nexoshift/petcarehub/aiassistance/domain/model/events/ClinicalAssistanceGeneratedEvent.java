package com.nexoshift.petcarehub.aiassistance.domain.model.events;

/** A new SOAP draft was generated. Medical History can listen to it later. */
public record ClinicalAssistanceGeneratedEvent(Long clinicalAssistanceId, Long consultationId) {
}

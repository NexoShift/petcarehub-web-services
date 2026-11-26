package com.nexoshift.petcarehub.aiassistance.domain.model.commands;

public record RegenerateClinicalAssistanceCommand(Long clinicalAssistanceId, Long veterinarianId) {
}

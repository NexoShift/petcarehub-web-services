package com.nexoshift.petcarehub.clinicalattention.domain.model.commands;

import com.nexoshift.petcarehub.clinicalattention.domain.model.valueobjects.ClinicalFieldLimits;

import java.time.LocalDateTime;

public record RegisterTreatmentCommand(
        Long consultationId, Long requesterUserId, String description, String instruction,
        LocalDateTime startDateTime, LocalDateTime endDateTime) {
    public RegisterTreatmentCommand {
        CommandValidation.requiredId(consultationId, "Consultation id");
        CommandValidation.requiredId(requesterUserId, "Requester user id");
        description = CommandValidation.requiredText(
                description, ClinicalFieldLimits.TREATMENT_DESCRIPTION, "Treatment description");
        instruction = CommandValidation.requiredText(
                instruction, ClinicalFieldLimits.TREATMENT_INSTRUCTION, "Treatment instruction");
        if (startDateTime == null) {
            throw new IllegalArgumentException("Start date and time are required");
        }
        if (endDateTime == null) {
            throw new IllegalArgumentException("End date and time are required");
        }
    }
}

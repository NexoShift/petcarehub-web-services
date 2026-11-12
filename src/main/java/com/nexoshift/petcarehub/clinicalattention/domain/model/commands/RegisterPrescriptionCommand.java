package com.nexoshift.petcarehub.clinicalattention.domain.model.commands;

import com.nexoshift.petcarehub.clinicalattention.domain.model.valueobjects.ClinicalFieldLimits;

import java.time.LocalDateTime;
import java.util.List;

public record RegisterPrescriptionCommand(
        Long consultationId, Long requesterUserId, LocalDateTime dateAndTimeOfIssuance,
        String generalInstruction, List<RegisterMedicationCommand> medications) {
    public RegisterPrescriptionCommand {
        CommandValidation.requiredId(consultationId, "Consultation id");
        CommandValidation.requiredId(requesterUserId, "Requester user id");

        if (dateAndTimeOfIssuance == null) {
            throw new IllegalArgumentException("Date and time of issuance are required");
        }

        generalInstruction = CommandValidation.requiredText(
                generalInstruction, ClinicalFieldLimits.PRESCRIPTION_INSTRUCTION, "General instruction");

        if (medications == null || medications.isEmpty()) {
            throw new IllegalArgumentException("A prescription must contain at least one medication");
        }
    }
}

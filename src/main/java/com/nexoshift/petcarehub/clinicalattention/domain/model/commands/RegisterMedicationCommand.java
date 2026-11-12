package com.nexoshift.petcarehub.clinicalattention.domain.model.commands;

import com.nexoshift.petcarehub.clinicalattention.domain.model.valueobjects.ClinicalFieldLimits;

public record RegisterMedicationCommand(
        String name, String dosage, String frequency, String duration, String indication) {
    public RegisterMedicationCommand {
        name = CommandValidation.requiredText(
                name, ClinicalFieldLimits.MEDICATION_NAME, "Medication name");
        dosage = CommandValidation.requiredText(
                dosage, ClinicalFieldLimits.MEDICATION_DOSAGE, "Medication dosage");
        frequency = CommandValidation.requiredText(
                frequency, ClinicalFieldLimits.MEDICATION_FREQUENCY, "Medication frequency");
        duration = CommandValidation.requiredText(
                duration, ClinicalFieldLimits.MEDICATION_DURATION, "Medication duration");
        indication = CommandValidation.requiredText(
                indication, ClinicalFieldLimits.MEDICATION_INDICATIONS, "Medication indication");
    }
}

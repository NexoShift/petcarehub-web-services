package com.nexoshift.petcarehub.clinicalattention.interfaces.rest.resources;

import com.nexoshift.petcarehub.clinicalattention.domain.model.valueobjects.ClinicalFieldLimits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterMedicationResource(
        @NotBlank @Size(max = ClinicalFieldLimits.MEDICATION_NAME) String name,
        @NotBlank @Size(max = ClinicalFieldLimits.MEDICATION_DOSAGE) String dosage,
        @NotBlank @Size(max = ClinicalFieldLimits.MEDICATION_FREQUENCY) String frequency,
        @NotBlank @Size(max = ClinicalFieldLimits.MEDICATION_DURATION) String duration,
        @NotBlank @Size(max = ClinicalFieldLimits.MEDICATION_INDICATIONS) String indication) {
}

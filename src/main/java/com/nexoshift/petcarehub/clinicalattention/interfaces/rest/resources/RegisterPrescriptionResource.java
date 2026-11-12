package com.nexoshift.petcarehub.clinicalattention.interfaces.rest.resources;

import com.nexoshift.petcarehub.clinicalattention.domain.model.valueobjects.ClinicalFieldLimits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.List;

public record RegisterPrescriptionResource(
        LocalDateTime dateAndTimeOfIssuance,
        @NotBlank @Size(max = ClinicalFieldLimits.PRESCRIPTION_INSTRUCTION) String generalInstruction,
        List<RegisterMedicationResource> medications
) {
}

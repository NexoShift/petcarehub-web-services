package com.nexoshift.petcarehub.medicalhistory.interfaces.rest.resources;

import com.nexoshift.petcarehub.medicalhistory.domain.model.valueobjects.AllergySeverity;
import com.nexoshift.petcarehub.medicalhistory.domain.model.valueobjects.MedicalHistoryFieldLimits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record RecordAllergyResource(
    @NotBlank @Size(max = MedicalHistoryFieldLimits.ALLERGEN) String allergen,
    @Size(max = MedicalHistoryFieldLimits.REACTION) String reaction,
    @NotNull AllergySeverity severity,
    @Size(max = MedicalHistoryFieldLimits.NOTES) String notes) {
}

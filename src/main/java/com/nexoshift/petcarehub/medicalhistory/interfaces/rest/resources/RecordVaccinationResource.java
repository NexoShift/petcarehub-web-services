package com.nexoshift.petcarehub.medicalhistory.interfaces.rest.resources;

import com.nexoshift.petcarehub.medicalhistory.domain.model.valueobjects.MedicalHistoryFieldLimits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/** Dates as yyyy-MM-dd. {@code nextDoseOn} is the booster date, if any. */
public record RecordVaccinationResource(
    @NotBlank @Size(max = MedicalHistoryFieldLimits.VACCINE_NAME) String vaccineName,
    @Min(1) @Max(MedicalHistoryFieldLimits.MAX_DOSE_NUMBER) Integer doseNumber,
    @NotNull @PastOrPresent LocalDate appliedOn,
    LocalDate nextDoseOn,
    @Size(max = MedicalHistoryFieldLimits.BATCH_NUMBER) String batchNumber,
    @Size(max = MedicalHistoryFieldLimits.NOTES) String notes) {
}

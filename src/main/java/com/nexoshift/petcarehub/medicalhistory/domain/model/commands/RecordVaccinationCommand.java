package com.nexoshift.petcarehub.medicalhistory.domain.model.commands;

import com.nexoshift.petcarehub.medicalhistory.domain.model.valueobjects.MedicalHistoryFieldLimits;

import java.time.LocalDate;

/** Only a veterinarian of a clinic where the pet is a patient. */
public record RecordVaccinationCommand(Long petId, Long requesterUserId, String vaccineName,
                                       Integer doseNumber, LocalDate appliedOn,
                                       LocalDate nextDoseOn, String batchNumber, String notes) {

  public RecordVaccinationCommand {
    TextValidation.requiredId(petId, "Pet id");
    TextValidation.requiredId(requesterUserId, "Requester user id");
    vaccineName = TextValidation.requiredText(vaccineName,
        MedicalHistoryFieldLimits.VACCINE_NAME, "Vaccine name");
    if (appliedOn == null) {
      throw new IllegalArgumentException("Application date is required");
    }
    batchNumber = TextValidation.optionalText(batchNumber,
        MedicalHistoryFieldLimits.BATCH_NUMBER, "Batch number");
    notes = TextValidation.optionalText(notes, MedicalHistoryFieldLimits.NOTES, "Notes");
  }
}

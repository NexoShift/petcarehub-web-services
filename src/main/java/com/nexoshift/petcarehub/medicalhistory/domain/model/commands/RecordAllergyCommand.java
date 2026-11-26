package com.nexoshift.petcarehub.medicalhistory.domain.model.commands;

import com.nexoshift.petcarehub.medicalhistory.domain.model.valueobjects.AllergySeverity;
import com.nexoshift.petcarehub.medicalhistory.domain.model.valueobjects.MedicalHistoryFieldLimits;

/** The owner of the pet or a veterinarian of a clinic where it is a patient. */
public record RecordAllergyCommand(Long petId, Long requesterUserId, String allergen,
                                   String reaction, AllergySeverity severity, String notes) {

  public RecordAllergyCommand {
    TextValidation.requiredId(petId, "Pet id");
    TextValidation.requiredId(requesterUserId, "Requester user id");
    allergen = TextValidation.requiredText(allergen, MedicalHistoryFieldLimits.ALLERGEN,
        "Allergen");
    reaction = TextValidation.optionalText(reaction, MedicalHistoryFieldLimits.REACTION,
        "Reaction");
    if (severity == null) {
      throw new IllegalArgumentException("Allergy severity is required");
    }
    notes = TextValidation.optionalText(notes, MedicalHistoryFieldLimits.NOTES, "Notes");
  }
}

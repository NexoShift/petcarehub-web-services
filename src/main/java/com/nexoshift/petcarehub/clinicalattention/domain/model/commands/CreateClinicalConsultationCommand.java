package com.nexoshift.petcarehub.clinicalattention.domain.model.commands;

import com.nexoshift.petcarehub.clinicalattention.domain.model.valueobjects.ClinicalFieldLimits;

/**
 * Starts the paperwork of a consultation. The veterinarian is the authenticated user: the
 * application service resolves the veterinarian id from {@code veterinarianUserId}.
 */
public record CreateClinicalConsultationCommand(
    Long petId, Long veterinarianUserId, Long clinicId, Long consultingRoomId, String reason) {

  public CreateClinicalConsultationCommand {
    CommandValidation.requiredId(petId, "Pet id");
    CommandValidation.requiredId(veterinarianUserId, "Veterinarian user id");
    CommandValidation.requiredId(clinicId, "Clinic id");
    CommandValidation.optionalId(consultingRoomId, "Consulting room id");
    reason = CommandValidation.requiredText(reason, ClinicalFieldLimits.REASON, "Reason");
  }
}

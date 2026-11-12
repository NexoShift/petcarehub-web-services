package com.nexoshift.petcarehub.clinicalattention.domain.model.commands;

import com.nexoshift.petcarehub.clinicalattention.domain.model.valueobjects.ClinicalFieldLimits;
import com.nexoshift.petcarehub.clinicalattention.domain.model.valueobjects.HydrationStatus;
import java.math.BigDecimal;

/**
 * One measurement of the pet. The four vital signs are required; every new request creates a new
 * assessment, so earlier measurements are never overwritten.
 */
public record RegisterPhysiologicalAssessmentCommand(
    Long consultationId, Long requesterUserId, BigDecimal weightKg, BigDecimal temperatureCelsius,
    Integer heartRateBpm, Integer respiratoryRateBpm, Integer bodyConditionScore,
    HydrationStatus hydrationStatus, String mucousMembranes,
    BigDecimal capillaryRefillTimeSeconds, String observations) {

  public RegisterPhysiologicalAssessmentCommand {
    CommandValidation.requiredId(consultationId, "Consultation id");
    CommandValidation.requiredId(requesterUserId, "Requester user id");
    CommandValidation.requiredValue(weightKg, "Weight (kg)");
    CommandValidation.requiredValue(temperatureCelsius, "Temperature (Celsius)");
    CommandValidation.requiredValue(heartRateBpm, "Heart rate (bpm)");
    CommandValidation.requiredValue(respiratoryRateBpm, "Respiratory rate (bpm)");
    mucousMembranes = CommandValidation.optionalText(
        mucousMembranes, ClinicalFieldLimits.MUCOUS_MEMBRANES, "Mucous membranes");
    if (capillaryRefillTimeSeconds != null && capillaryRefillTimeSeconds.signum() <= 0) {
      throw new IllegalArgumentException("Capillary refill time (seconds) must be greater than 0");
    }
    observations = CommandValidation.optionalText(
        observations, ClinicalFieldLimits.ASSESSMENT_OBSERVATIONS, "Observations");
  }
}

package com.nexoshift.petcarehub.clinicalattention.domain.model.commands;

import com.nexoshift.petcarehub.clinicalattention.domain.model.valueobjects.ClinicalFieldLimits;
import com.nexoshift.petcarehub.clinicalattention.domain.model.valueobjects.DiagnosisType;
import java.util.List;

/**
 * Reevaluates a diagnosis. A new diagnosis is created and the previous one is kept, marked as
 * superseded, without changing its content (CON-01).
 */
public record ReevaluateDiagnosisCommand(
    Long consultationId, Long previousDiagnosisId, Long requesterUserId, String description,
    DiagnosisType type, List<String> differentialDiagnoses, String rationale) {

  public ReevaluateDiagnosisCommand {
    CommandValidation.requiredId(consultationId, "Consultation id");
    CommandValidation.requiredId(previousDiagnosisId, "Previous diagnosis id");
    CommandValidation.requiredId(requesterUserId, "Requester user id");
    description = CommandValidation.requiredText(
        description, ClinicalFieldLimits.DIAGNOSIS_DESCRIPTION, "Diagnosis description");
    CommandValidation.requiredValue(type, "Diagnosis type");
    differentialDiagnoses = CommandValidation.textList(differentialDiagnoses,
        ClinicalFieldLimits.MAX_DIFFERENTIAL_DIAGNOSES, ClinicalFieldLimits.DIFFERENTIAL_DIAGNOSIS,
        "Differential diagnoses");
    rationale = CommandValidation.optionalText(
        rationale, ClinicalFieldLimits.RATIONALE, "Rationale");
  }
}

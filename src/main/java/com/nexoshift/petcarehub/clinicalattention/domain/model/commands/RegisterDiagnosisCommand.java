package com.nexoshift.petcarehub.clinicalattention.domain.model.commands;

import com.nexoshift.petcarehub.clinicalattention.domain.model.valueobjects.ClinicalFieldLimits;
import com.nexoshift.petcarehub.clinicalattention.domain.model.valueobjects.DiagnosisType;
import java.util.List;

/** A consultation has at most one current primary diagnosis; the others are secondary. */
public record RegisterDiagnosisCommand(
    Long consultationId, Long requesterUserId, String description, DiagnosisType type,
    boolean primary, List<String> differentialDiagnoses, String rationale) {

  public RegisterDiagnosisCommand {
    CommandValidation.requiredId(consultationId, "Consultation id");
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

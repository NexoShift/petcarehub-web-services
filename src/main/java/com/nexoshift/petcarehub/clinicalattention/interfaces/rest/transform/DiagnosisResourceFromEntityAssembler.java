package com.nexoshift.petcarehub.clinicalattention.interfaces.rest.transform;

import com.nexoshift.petcarehub.clinicalattention.domain.model.entities.Diagnosis;
import com.nexoshift.petcarehub.clinicalattention.interfaces.rest.resources.DiagnosisResource;

public final class DiagnosisResourceFromEntityAssembler {

  private DiagnosisResourceFromEntityAssembler() {
  }

  public static DiagnosisResource toResourceFromEntity(Diagnosis diagnosis) {
    return new DiagnosisResource(diagnosis.getId(), diagnosis.getDescription(),
        diagnosis.getType(), diagnosis.isPrimary(), diagnosis.getDifferentialDiagnoses(),
        diagnosis.getRationale(), diagnosis.getStatus(), diagnosis.getPreviousDiagnosisId(),
        diagnosis.getDiagnosedBy().veterinarianId(), diagnosis.getDiagnosedAt());
  }
}

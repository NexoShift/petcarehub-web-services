package com.nexoshift.petcarehub.clinicalattention.domain.services;

import com.nexoshift.petcarehub.clinicalattention.domain.model.aggregates.ClinicalConsultation;
import com.nexoshift.petcarehub.clinicalattention.domain.model.commands.AddClinicalFindingCommand;
import com.nexoshift.petcarehub.clinicalattention.domain.model.commands.CancelClinicalConsultationCommand;
import com.nexoshift.petcarehub.clinicalattention.domain.model.commands.CompleteClinicalConsultationCommand;
import com.nexoshift.petcarehub.clinicalattention.domain.model.commands.CreateClinicalConsultationCommand;
import com.nexoshift.petcarehub.clinicalattention.domain.model.commands.ReevaluateDiagnosisCommand;
import com.nexoshift.petcarehub.clinicalattention.domain.model.commands.RegisterAnamnesisCommand;
import com.nexoshift.petcarehub.clinicalattention.domain.model.commands.RegisterDiagnosisCommand;
import com.nexoshift.petcarehub.clinicalattention.domain.model.commands.RegisterPhysiologicalAssessmentCommand;
import com.nexoshift.petcarehub.clinicalattention.domain.model.commands.StartClinicalConsultationCommand;
import java.util.Optional;

/** Use cases that change a clinical consultation. Each one returns the saved aggregate. */
public interface ClinicalConsultationCommandService {

  Optional<ClinicalConsultation> handle(CreateClinicalConsultationCommand command);

  Optional<ClinicalConsultation> handle(StartClinicalConsultationCommand command);

  Optional<ClinicalConsultation> handle(RegisterAnamnesisCommand command);

  Optional<ClinicalConsultation> handle(RegisterPhysiologicalAssessmentCommand command);

  Optional<ClinicalConsultation> handle(AddClinicalFindingCommand command);

  Optional<ClinicalConsultation> handle(RegisterDiagnosisCommand command);

  Optional<ClinicalConsultation> handle(ReevaluateDiagnosisCommand command);

  Optional<ClinicalConsultation> handle(CompleteClinicalConsultationCommand command);

  Optional<ClinicalConsultation> handle(CancelClinicalConsultationCommand command);
}

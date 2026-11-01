package com.nexoshift.petcarehub.clinicalattention.support;

import com.nexoshift.petcarehub.clinicalattention.domain.model.aggregates.ClinicalConsultation;
import com.nexoshift.petcarehub.clinicalattention.domain.model.commands.AddClinicalFindingCommand;
import com.nexoshift.petcarehub.clinicalattention.domain.model.commands.CreateClinicalConsultationCommand;
import com.nexoshift.petcarehub.clinicalattention.domain.model.commands.ReevaluateDiagnosisCommand;
import com.nexoshift.petcarehub.clinicalattention.domain.model.commands.RegisterAnamnesisCommand;
import com.nexoshift.petcarehub.clinicalattention.domain.model.commands.RegisterDiagnosisCommand;
import com.nexoshift.petcarehub.clinicalattention.domain.model.commands.RegisterPhysiologicalAssessmentCommand;
import com.nexoshift.petcarehub.clinicalattention.domain.model.valueobjects.BodySystem;
import com.nexoshift.petcarehub.clinicalattention.domain.model.valueobjects.DiagnosisType;
import com.nexoshift.petcarehub.clinicalattention.domain.model.valueobjects.FindingSeverity;
import com.nexoshift.petcarehub.clinicalattention.domain.model.valueobjects.HydrationStatus;
import com.nexoshift.petcarehub.clinicalattention.domain.model.valueobjects.VeterinarianId;
import java.math.BigDecimal;
import java.util.List;

/** Valid data for the tests. Ids: user 10, veterinarian 100, pet 5, clinic 3, consultation 1. */
public final class ClinicalFixtures {

  public static final long USER_ID = 10L;
  public static final long VETERINARIAN_ID = 100L;
  public static final long OTHER_USER_ID = 11L;
  public static final long OTHER_VETERINARIAN_ID = 101L;
  public static final long OWNER_USER_ID = 20L;
  public static final long PET_ID = 5L;
  public static final long CLINIC_ID = 3L;
  public static final long ROOM_ID = 7L;
  public static final long CONSULTATION_ID = 1L;

  private ClinicalFixtures() {
  }

  public static CreateClinicalConsultationCommand createCommand() {
    return new CreateClinicalConsultationCommand(
        PET_ID, USER_ID, CLINIC_ID, ROOM_ID, "Vomits and does not eat since yesterday");
  }

  public static RegisterAnamnesisCommand anamnesisCommand(Long consultationId) {
    return new RegisterAnamnesisCommand(consultationId, USER_ID, "Vomiting", "2 days",
        List.of("Vomiting", "Lethargy"), "Dry food", "Calm", "Deworming last month",
        "The owner changed the food last week");
  }

  public static RegisterPhysiologicalAssessmentCommand assessmentCommand(Long consultationId) {
    return new RegisterPhysiologicalAssessmentCommand(consultationId, USER_ID,
        new BigDecimal("12.50"), new BigDecimal("38.6"), 110, 28, 5,
        HydrationStatus.MILD_DEHYDRATION, "Pale pink", new BigDecimal("2.0"), "Quiet but alert");
  }

  public static AddClinicalFindingCommand findingCommand(Long consultationId) {
    return new AddClinicalFindingCommand(consultationId, USER_ID, BodySystem.DIGESTIVE,
        "Painful abdomen on palpation", FindingSeverity.MODERATE);
  }

  public static RegisterDiagnosisCommand primaryDiagnosisCommand(Long consultationId) {
    return new RegisterDiagnosisCommand(consultationId, USER_ID, "Acute gastroenteritis",
        DiagnosisType.PRESUMPTIVE, true, List.of("Foreign body", "Pancreatitis"),
        "Vomiting and abdominal pain after a food change");
  }

  public static RegisterDiagnosisCommand secondaryDiagnosisCommand(Long consultationId) {
    return new RegisterDiagnosisCommand(consultationId, USER_ID, "Mild dehydration",
        DiagnosisType.DEFINITIVE, false, List.of(), null);
  }

  public static ReevaluateDiagnosisCommand reevaluationCommand(
      Long consultationId, Long previousDiagnosisId) {
    return new ReevaluateDiagnosisCommand(consultationId, previousDiagnosisId, USER_ID,
        "Pancreatitis", DiagnosisType.DEFINITIVE, List.of("Gastroenteritis"),
        "Lipase elevated in the blood test");
  }

  /** CREATED consultation with id 1, attended by veterinarian 100. */
  public static ClinicalConsultation newConsultation() {
    var consultation = new ClinicalConsultation(createCommand(), new VeterinarianId(VETERINARIAN_ID));
    ClinicalTestSupport.withId(consultation, CONSULTATION_ID);
    return consultation;
  }

  /** IN_PROGRESS consultation with id 1. */
  public static ClinicalConsultation inProgressConsultation() {
    var consultation = newConsultation();
    consultation.start();
    return consultation;
  }

  /** IN_PROGRESS consultation with anamnesis, one assessment and a primary diagnosis. */
  public static ClinicalConsultation completableConsultation() {
    var consultation = inProgressConsultation();
    consultation.registerAnamnesis(anamnesisCommand(CONSULTATION_ID));
    consultation.registerPhysiologicalAssessment(assessmentCommand(CONSULTATION_ID));
    consultation.registerDiagnosis(primaryDiagnosisCommand(CONSULTATION_ID));
    return consultation;
  }
}

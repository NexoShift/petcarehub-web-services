package com.nexoshift.petcarehub.clinicalattention.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.nexoshift.petcarehub.clinicalattention.domain.model.commands.AddClinicalFindingCommand;
import com.nexoshift.petcarehub.clinicalattention.domain.model.commands.CancelClinicalConsultationCommand;
import com.nexoshift.petcarehub.clinicalattention.domain.model.commands.CompleteClinicalConsultationCommand;
import com.nexoshift.petcarehub.clinicalattention.domain.model.commands.CreateClinicalConsultationCommand;
import com.nexoshift.petcarehub.clinicalattention.domain.model.commands.ReevaluateDiagnosisCommand;
import com.nexoshift.petcarehub.clinicalattention.domain.model.commands.RegisterAnamnesisCommand;
import com.nexoshift.petcarehub.clinicalattention.domain.model.commands.RegisterDiagnosisCommand;
import com.nexoshift.petcarehub.clinicalattention.domain.model.commands.RegisterPhysiologicalAssessmentCommand;
import com.nexoshift.petcarehub.clinicalattention.domain.model.commands.StartClinicalConsultationCommand;
import com.nexoshift.petcarehub.clinicalattention.domain.model.valueobjects.BodySystem;
import com.nexoshift.petcarehub.clinicalattention.domain.model.valueobjects.DiagnosisType;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** The commands reject invalid data before it reaches the aggregate. */
class ClinicalCommandsTest {

  private static String text(int length) {
    return "x".repeat(length);
  }

  private static RegisterAnamnesisCommand anamnesis(
      String chiefComplaint, String evolutionTime, List<String> symptoms, String diet) {
    return new RegisterAnamnesisCommand(1L, 10L, chiefComplaint, evolutionTime, symptoms, diet,
        null, null, null);
  }

  private static RegisterPhysiologicalAssessmentCommand assessment(
      BigDecimal weight, BigDecimal temperature, Integer heartRate, Integer respiratoryRate,
      BigDecimal capillaryRefill, String observations) {
    return new RegisterPhysiologicalAssessmentCommand(1L, 10L, weight, temperature, heartRate,
        respiratoryRate, null, null, null, capillaryRefill, observations);
  }

  // ------------------------------------------------------------------ create

  @Test
  @DisplayName("Create: the reason is trimmed and the consulting room is optional")
  void createNormalizesData() {
    var command = new CreateClinicalConsultationCommand(5L, 10L, 3L, null, "  Check-up  ");

    assertThat(command.reason()).isEqualTo("Check-up");
    assertThat(command.consultingRoomId()).isNull();
  }

  @Test
  @DisplayName("Create: reason, pet, veterinarian user and clinic are required")
  void createRequiredData() {
    assertThatThrownBy(() -> new CreateClinicalConsultationCommand(5L, 10L, 3L, null, " "))
        .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("Reason");
    assertThatThrownBy(() -> new CreateClinicalConsultationCommand(5L, 10L, 3L, null, null))
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> new CreateClinicalConsultationCommand(null, 10L, 3L, null, "Reason"))
        .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("Pet id");
    assertThatThrownBy(() -> new CreateClinicalConsultationCommand(0L, 10L, 3L, null, "Reason"))
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> new CreateClinicalConsultationCommand(5L, null, 3L, null, "Reason"))
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> new CreateClinicalConsultationCommand(5L, 10L, null, null, "Reason"))
        .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("Clinic id");
  }

  @Test
  @DisplayName("Create: a consulting room id, when given, must be positive; the reason has a limit")
  void createLimits() {
    assertThatThrownBy(() -> new CreateClinicalConsultationCommand(5L, 10L, 3L, 0L, "Reason"))
        .isInstanceOf(IllegalArgumentException.class);
    new CreateClinicalConsultationCommand(5L, 10L, 3L, 1L, text(350));
    assertThatThrownBy(() -> new CreateClinicalConsultationCommand(5L, 10L, 3L, null, text(351)))
        .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("350");
  }

  // ------------------------------------------------------- start/complete/cancel

  @Test
  @DisplayName("Start and complete need the consultation and the requester")
  void startAndCompleteNeedIds() {
    assertThatThrownBy(() -> new StartClinicalConsultationCommand(null, 10L))
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> new StartClinicalConsultationCommand(1L, null))
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> new CompleteClinicalConsultationCommand(0L, 10L))
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> new CompleteClinicalConsultationCommand(1L, -1L))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  @DisplayName("Cancel: the reason is required, trimmed and limited")
  void cancelReason() {
    assertThat(new CancelClinicalConsultationCommand(1L, 10L, "  No show ").reason())
        .isEqualTo("No show");
    assertThatThrownBy(() -> new CancelClinicalConsultationCommand(1L, 10L, " "))
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> new CancelClinicalConsultationCommand(1L, 10L, text(501)))
        .isInstanceOf(IllegalArgumentException.class);
  }

  // --------------------------------------------------------------- anamnesis

  @Test
  @DisplayName("Anamnesis: chief complaint and evolution time are required")
  void anamnesisRequiredData() {
    assertThatThrownBy(() -> anamnesis(" ", "2 days", null, null))
        .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("Chief complaint");
    assertThatThrownBy(() -> anamnesis("Vomiting", null, null, null))
        .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("Evolution time");
  }

  @Test
  @DisplayName("Anamnesis: symptoms are trimmed, without blanks or repeated items, in order")
  void anamnesisSymptomsAreNormalized() {
    var command = anamnesis("Vomiting", "2 days",
        Arrays.asList(" Vomiting ", "", null, "Lethargy", "Vomiting", "   "), null);

    assertThat(command.symptoms()).containsExactly("Vomiting", "Lethargy");
  }

  @Test
  @DisplayName("Anamnesis: no symptoms is an empty list and blank optional texts become null")
  void anamnesisOptionalData() {
    var command = anamnesis("Vomiting", "2 days", null, "   ");

    assertThat(command.symptoms()).isEmpty();
    assertThat(command.diet()).isNull();
    assertThat(command.behavior()).isNull();
    assertThat(command.previousTreatments()).isNull();
    assertThat(command.ownerObservations()).isNull();
  }

  @Test
  @DisplayName("Anamnesis: texts and the list of symptoms have limits")
  void anamnesisLimits() {
    var tooManySymptoms = new ArrayList<String>();
    for (int i = 0; i <= 20; i++) {
      tooManySymptoms.add("Symptom " + i);
    }
    assertThatThrownBy(() -> anamnesis("Vomiting", "2 days", tooManySymptoms, null))
        .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("20");
    assertThatThrownBy(() -> anamnesis("Vomiting", "2 days", List.of(text(101)), null))
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> anamnesis(text(351), "2 days", null, null))
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> anamnesis("Vomiting", text(101), null, null))
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> anamnesis("Vomiting", "2 days", null, text(501)))
        .isInstanceOf(IllegalArgumentException.class);
  }

  // ------------------------------------------------- physiological assessment

  @Test
  @DisplayName("Assessment: the four vital signs are required")
  void assessmentRequiredData() {
    var weight = new BigDecimal("10");
    var temperature = new BigDecimal("38.5");
    assertThatThrownBy(() -> assessment(null, temperature, 100, 20, null, null))
        .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("Weight");
    assertThatThrownBy(() -> assessment(weight, null, 100, 20, null, null))
        .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("Temperature");
    assertThatThrownBy(() -> assessment(weight, temperature, null, 20, null, null))
        .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("Heart rate");
    assertThatThrownBy(() -> assessment(weight, temperature, 100, null, null, null))
        .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("Respiratory rate");
  }

  @Test
  @DisplayName("Assessment: capillary refill time must be positive and observations are limited")
  void assessmentLimits() {
    var weight = new BigDecimal("10");
    var temperature = new BigDecimal("38.5");
    assertThatThrownBy(() -> assessment(weight, temperature, 100, 20, BigDecimal.ZERO, null))
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> assessment(weight, temperature, 100, 20, null, text(2001)))
        .isInstanceOf(IllegalArgumentException.class);
    assertThat(assessment(weight, temperature, 100, 20, new BigDecimal("1.5"), "  ok ")
        .observations()).isEqualTo("ok");
  }

  // ---------------------------------------------------------------- findings

  @Test
  @DisplayName("Finding: body system and description are required, severity is optional")
  void findingRequiredData() {
    assertThatThrownBy(() -> new AddClinicalFindingCommand(1L, 10L, null, "Description", null))
        .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("Body system");
    assertThatThrownBy(() -> new AddClinicalFindingCommand(1L, 10L, BodySystem.OCULAR, " ", null))
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(
        () -> new AddClinicalFindingCommand(1L, 10L, BodySystem.OCULAR, text(1001), null))
        .isInstanceOf(IllegalArgumentException.class);
    assertThat(new AddClinicalFindingCommand(1L, 10L, BodySystem.OCULAR, " Red eye ", null)
        .description()).isEqualTo("Red eye");
  }

  // --------------------------------------------------------------- diagnoses

  @Test
  @DisplayName("Diagnosis: description and type are required")
  void diagnosisRequiredData() {
    assertThatThrownBy(() -> new RegisterDiagnosisCommand(
        1L, 10L, " ", DiagnosisType.PRESUMPTIVE, true, null, null))
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> new RegisterDiagnosisCommand(1L, 10L, "Gastritis", null, true, null, null))
        .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("Diagnosis type");
  }

  @Test
  @DisplayName("Diagnosis: differential diagnoses are normalized and limited")
  void diagnosisDifferentials() {
    var command = new RegisterDiagnosisCommand(1L, 10L, "Gastritis", DiagnosisType.PRESUMPTIVE,
        false, Arrays.asList(" Pancreatitis ", "Pancreatitis", "", "Parasites"), "  because  ");

    assertThat(command.differentialDiagnoses()).containsExactly("Pancreatitis", "Parasites");
    assertThat(command.rationale()).isEqualTo("because");
    var tooMany = new ArrayList<String>();
    for (int i = 0; i <= 10; i++) {
      tooMany.add("Diagnosis " + i);
    }
    assertThatThrownBy(() -> new RegisterDiagnosisCommand(
        1L, 10L, "Gastritis", DiagnosisType.PRESUMPTIVE, false, tooMany, null))
        .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("10");
    assertThatThrownBy(() -> new RegisterDiagnosisCommand(
        1L, 10L, text(501), DiagnosisType.PRESUMPTIVE, false, null, null))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  @DisplayName("Reevaluation: it needs the diagnosis to reevaluate, a description and a type")
  void reevaluationRequiredData() {
    assertThatThrownBy(() -> new ReevaluateDiagnosisCommand(
        1L, null, 10L, "Pancreatitis", DiagnosisType.DEFINITIVE, null, null))
        .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("Previous diagnosis id");
    assertThatThrownBy(() -> new ReevaluateDiagnosisCommand(
        1L, 5L, 10L, "", DiagnosisType.DEFINITIVE, null, null))
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> new ReevaluateDiagnosisCommand(
        1L, 5L, 10L, "Pancreatitis", null, null, null))
        .isInstanceOf(IllegalArgumentException.class);
    var command = new ReevaluateDiagnosisCommand(
        1L, 5L, 10L, "Pancreatitis", DiagnosisType.DEFINITIVE, null, null);
    assertThat(command.differentialDiagnoses()).isEmpty();
  }
}

package com.nexoshift.petcarehub.clinicalattention.domain;

import static com.nexoshift.petcarehub.clinicalattention.support.ClinicalFixtures.CONSULTATION_ID;
import static com.nexoshift.petcarehub.clinicalattention.support.ClinicalFixtures.USER_ID;
import static com.nexoshift.petcarehub.clinicalattention.support.ClinicalFixtures.anamnesisCommand;
import static com.nexoshift.petcarehub.clinicalattention.support.ClinicalFixtures.assessmentCommand;
import static com.nexoshift.petcarehub.clinicalattention.support.ClinicalFixtures.completableConsultation;
import static com.nexoshift.petcarehub.clinicalattention.support.ClinicalFixtures.createCommand;
import static com.nexoshift.petcarehub.clinicalattention.support.ClinicalFixtures.findingCommand;
import static com.nexoshift.petcarehub.clinicalattention.support.ClinicalFixtures.inProgressConsultation;
import static com.nexoshift.petcarehub.clinicalattention.support.ClinicalFixtures.newConsultation;
import static com.nexoshift.petcarehub.clinicalattention.support.ClinicalFixtures.primaryDiagnosisCommand;
import static com.nexoshift.petcarehub.clinicalattention.support.ClinicalFixtures.reevaluationCommand;
import static com.nexoshift.petcarehub.clinicalattention.support.ClinicalFixtures.secondaryDiagnosisCommand;
import static com.nexoshift.petcarehub.clinicalattention.support.ClinicalTestSupport.clearDomainEvents;
import static com.nexoshift.petcarehub.clinicalattention.support.ClinicalTestSupport.domainEvents;
import static com.nexoshift.petcarehub.clinicalattention.support.ClinicalTestSupport.withId;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.nexoshift.petcarehub.clinicalattention.domain.exceptions.IncompleteConsultationException;
import com.nexoshift.petcarehub.clinicalattention.domain.exceptions.InvalidConsultationStateException;
import com.nexoshift.petcarehub.clinicalattention.domain.model.aggregates.ClinicalConsultation;
import com.nexoshift.petcarehub.clinicalattention.domain.model.commands.CreateClinicalConsultationCommand;
import com.nexoshift.petcarehub.clinicalattention.domain.model.commands.RegisterPhysiologicalAssessmentCommand;
import com.nexoshift.petcarehub.clinicalattention.domain.model.events.ClinicalConsultationCancelledEvent;
import com.nexoshift.petcarehub.clinicalattention.domain.model.events.ClinicalConsultationCompletedEvent;
import com.nexoshift.petcarehub.clinicalattention.domain.model.events.ClinicalConsultationCreatedEvent;
import com.nexoshift.petcarehub.clinicalattention.domain.model.valueobjects.ConsultationStatus;
import com.nexoshift.petcarehub.clinicalattention.domain.model.valueobjects.DiagnosisStatus;
import com.nexoshift.petcarehub.clinicalattention.domain.model.valueobjects.DiagnosisType;
import com.nexoshift.petcarehub.clinicalattention.domain.model.valueobjects.VeterinarianId;
import java.math.BigDecimal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ClinicalConsultationTest {

  private static final long DIAGNOSIS_ID = 50L;

  // ---------------------------------------------------------------- creation

  @Test
  @DisplayName("A new consultation is CREATED and keeps the ids as value objects")
  void newConsultationIsCreated() {
    var consultation = newConsultation();

    assertThat(consultation.getStatus()).isEqualTo(ConsultationStatus.CREATED);
    assertThat(consultation.getPetId().petId()).isEqualTo(5L);
    assertThat(consultation.getVeterinarianId().veterinarianId()).isEqualTo(100L);
    assertThat(consultation.getClinicId().clinicId()).isEqualTo(3L);
    assertThat(consultation.getConsultingRoomId().consultingRoomId()).isEqualTo(7L);
    assertThat(consultation.getReason()).isEqualTo("Vomits and does not eat since yesterday");
    assertThat(consultation.getStartedAt()).isNull();
    assertThat(consultation.getCompletedAt()).isNull();
    assertThat(consultation.getCancellation()).isNull();
    assertThat(consultation.getAnamnesis()).isNull();
    assertThat(consultation.getPhysiologicalAssessments()).isEmpty();
    assertThat(consultation.getClinicalFindings()).isEmpty();
    assertThat(consultation.getDiagnoses()).isEmpty();
  }

  @Test
  @DisplayName("The consulting room is optional")
  void consultingRoomIsOptional() {
    var command = new CreateClinicalConsultationCommand(5L, USER_ID, 3L, null, "Check-up");

    var consultation = new ClinicalConsultation(command, new VeterinarianId(100L));

    assertThat(consultation.getConsultingRoomId()).isNull();
  }

  @Test
  @DisplayName("A consultation needs a veterinarian")
  void consultationNeedsVeterinarian() {
    assertThatThrownBy(
        () -> new ClinicalConsultation(createCommand(), null))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  @DisplayName("notifyCreated registers the created event with the generated id")
  void notifyCreatedRegistersEvent() {
    var consultation = newConsultation();

    consultation.notifyCreated();

    var events = domainEvents(consultation, ClinicalConsultationCreatedEvent.class);
    assertThat(events).hasSize(1);
    assertThat(events.get(0).consultationId()).isEqualTo(CONSULTATION_ID);
    assertThat(events.get(0).petId()).isEqualTo(5L);
    assertThat(events.get(0).veterinarianId()).isEqualTo(100L);
    assertThat(events.get(0).occurredAt()).isNotNull();
  }

  // ------------------------------------------------------------------- start

  @Test
  @DisplayName("start moves CREATED to IN_PROGRESS and stores the start time")
  void startMovesToInProgress() {
    var consultation = newConsultation();

    consultation.start();

    assertThat(consultation.getStatus()).isEqualTo(ConsultationStatus.IN_PROGRESS);
    assertThat(consultation.isInProgress()).isTrue();
    assertThat(consultation.getStartedAt()).isNotNull();
  }

  @Test
  @DisplayName("A consultation cannot be started twice")
  void cannotStartTwice() {
    var consultation = inProgressConsultation();

    assertThatThrownBy(consultation::start).isInstanceOf(InvalidConsultationStateException.class);
  }

  @Test
  @DisplayName("Records cannot be registered before the consultation starts")
  void cannotRegisterBeforeStart() {
    var consultation = newConsultation();

    assertThatThrownBy(() -> consultation.registerAnamnesis(anamnesisCommand(CONSULTATION_ID)))
        .isInstanceOf(InvalidConsultationStateException.class);
    assertThatThrownBy(
        () -> consultation.registerPhysiologicalAssessment(assessmentCommand(CONSULTATION_ID)))
        .isInstanceOf(InvalidConsultationStateException.class);
    assertThatThrownBy(() -> consultation.addClinicalFinding(findingCommand(CONSULTATION_ID)))
        .isInstanceOf(InvalidConsultationStateException.class);
    assertThatThrownBy(() -> consultation.registerDiagnosis(primaryDiagnosisCommand(CONSULTATION_ID)))
        .isInstanceOf(InvalidConsultationStateException.class);
  }

  // --------------------------------------------------------------- anamnesis

  @Test
  @DisplayName("The anamnesis is registered with all its data")
  void registersAnamnesis() {
    var consultation = inProgressConsultation();

    var anamnesis = consultation.registerAnamnesis(anamnesisCommand(CONSULTATION_ID));

    assertThat(consultation.getAnamnesis()).isSameAs(anamnesis);
    assertThat(anamnesis.getChiefComplaint()).isEqualTo("Vomiting");
    assertThat(anamnesis.getEvolutionTime()).isEqualTo("2 days");
    assertThat(anamnesis.getSymptoms()).containsExactly("Vomiting", "Lethargy");
    assertThat(anamnesis.getDiet()).isEqualTo("Dry food");
    assertThat(anamnesis.getBehavior()).isEqualTo("Calm");
    assertThat(anamnesis.getPreviousTreatments()).isEqualTo("Deworming last month");
    assertThat(anamnesis.getOwnerObservations()).isEqualTo("The owner changed the food last week");
  }

  @Test
  @DisplayName("The anamnesis cannot be registered twice (it is not overwritten)")
  void anamnesisIsRegisteredOnce() {
    var consultation = inProgressConsultation();
    var first = consultation.registerAnamnesis(anamnesisCommand(CONSULTATION_ID));

    assertThatThrownBy(() -> consultation.registerAnamnesis(anamnesisCommand(CONSULTATION_ID)))
        .isInstanceOf(InvalidConsultationStateException.class)
        .hasMessageContaining("already registered");
    assertThat(consultation.getAnamnesis()).isSameAs(first);
  }

  // ------------------------------------------------- physiological assessment

  @Test
  @DisplayName("Each physiological assessment is a new record and the earlier ones are kept")
  void assessmentsAreAppended() {
    var consultation = inProgressConsultation();

    var first = consultation.registerPhysiologicalAssessment(assessmentCommand(CONSULTATION_ID));
    var second = consultation.registerPhysiologicalAssessment(assessmentCommand(CONSULTATION_ID));

    assertThat(consultation.getPhysiologicalAssessments()).hasSize(2);
    assertThat(consultation.getPhysiologicalAssessments().get(0)).isSameAs(first);
    assertThat(consultation.getPhysiologicalAssessments().get(1)).isSameAs(second);
  }

  @Test
  @DisplayName("The assessment stores vital signs, body condition and hydration")
  void assessmentKeepsMeasurements() {
    var consultation = inProgressConsultation();

    var assessment = consultation.registerPhysiologicalAssessment(assessmentCommand(CONSULTATION_ID));

    assertThat(assessment.getVitalSigns().weightKg()).isEqualByComparingTo("12.50");
    assertThat(assessment.getVitalSigns().temperatureCelsius()).isEqualByComparingTo("38.6");
    assertThat(assessment.getVitalSigns().heartRateBpm()).isEqualTo(110);
    assertThat(assessment.getVitalSigns().respiratoryRateBpm()).isEqualTo(28);
    assertThat(assessment.getBodyConditionScore().score()).isEqualTo(5);
    assertThat(assessment.getMucousMembranes()).isEqualTo("Pale pink");
    assertThat(assessment.getCapillaryRefillTimeSeconds()).isEqualByComparingTo(BigDecimal.valueOf(2));
    assertThat(assessment.getObservations()).isEqualTo("Quiet but alert");
    assertThat(assessment.getRecordedAt()).isNotNull();
  }

  @Test
  @DisplayName("An assessment with a vital sign out of range is rejected and not stored")
  void invalidAssessmentIsRejected() {
    var consultation = inProgressConsultation();
    var command = new RegisterPhysiologicalAssessmentCommand(CONSULTATION_ID, USER_ID,
        new BigDecimal("12"), new BigDecimal("50"), 110, 28, null, null, null, null, null);

    assertThatThrownBy(() -> consultation.registerPhysiologicalAssessment(command))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Temperature");
    assertThat(consultation.getPhysiologicalAssessments()).isEmpty();
  }

  // ---------------------------------------------------------------- findings

  @Test
  @DisplayName("Clinical findings are added by body system")
  void addsFindings() {
    var consultation = inProgressConsultation();

    var finding = consultation.addClinicalFinding(findingCommand(CONSULTATION_ID));

    assertThat(consultation.getClinicalFindings()).containsExactly(finding);
    assertThat(finding.getBodySystem().name()).isEqualTo("DIGESTIVE");
    assertThat(finding.getDescription()).isEqualTo("Painful abdomen on palpation");
    assertThat(finding.getSeverity().name()).isEqualTo("MODERATE");
  }

  // --------------------------------------------------------------- diagnoses

  @Test
  @DisplayName("A primary diagnosis is registered as CURRENT")
  void registersPrimaryDiagnosis() {
    var consultation = inProgressConsultation();

    var diagnosis = consultation.registerDiagnosis(primaryDiagnosisCommand(CONSULTATION_ID));

    assertThat(consultation.getDiagnoses()).containsExactly(diagnosis);
    assertThat(diagnosis.getDescription()).isEqualTo("Acute gastroenteritis");
    assertThat(diagnosis.getType()).isEqualTo(DiagnosisType.PRESUMPTIVE);
    assertThat(diagnosis.isPrimary()).isTrue();
    assertThat(diagnosis.getStatus()).isEqualTo(DiagnosisStatus.CURRENT);
    assertThat(diagnosis.isCurrent()).isTrue();
    assertThat(diagnosis.isReevaluation()).isFalse();
    assertThat(diagnosis.getPreviousDiagnosisId()).isNull();
    assertThat(diagnosis.getDifferentialDiagnoses()).containsExactly("Foreign body", "Pancreatitis");
    assertThat(diagnosis.getDiagnosedBy().veterinarianId()).isEqualTo(100L);
    assertThat(diagnosis.getDiagnosedAt()).isNotNull();
  }

  @Test
  @DisplayName("There cannot be two current primary diagnoses")
  void onlyOneCurrentPrimaryDiagnosis() {
    var consultation = inProgressConsultation();
    consultation.registerDiagnosis(primaryDiagnosisCommand(CONSULTATION_ID));

    assertThatThrownBy(() -> consultation.registerDiagnosis(primaryDiagnosisCommand(CONSULTATION_ID)))
        .isInstanceOf(InvalidConsultationStateException.class)
        .hasMessageContaining("reevaluate");
    assertThat(consultation.getDiagnoses()).hasSize(1);
  }

  @Test
  @DisplayName("Secondary diagnoses can be added next to the primary one")
  void secondaryDiagnosesAreAllowed() {
    var consultation = inProgressConsultation();
    consultation.registerDiagnosis(primaryDiagnosisCommand(CONSULTATION_ID));

    var secondary = consultation.registerDiagnosis(secondaryDiagnosisCommand(CONSULTATION_ID));

    assertThat(consultation.getDiagnoses()).hasSize(2);
    assertThat(secondary.isPrimary()).isFalse();
    assertThat(secondary.getDifferentialDiagnoses()).isEmpty();
    assertThat(secondary.getRationale()).isNull();
  }

  // ------------------------------------------------------------ reevaluation

  @Test
  @DisplayName("Reevaluating creates a new diagnosis and keeps the previous one unchanged (CON-01)")
  void reevaluationKeepsPreviousDiagnosis() {
    var consultation = inProgressConsultation();
    var previous = withId(
        consultation.registerDiagnosis(primaryDiagnosisCommand(CONSULTATION_ID)), DIAGNOSIS_ID);

    var reevaluation = consultation.reevaluateDiagnosis(
        reevaluationCommand(CONSULTATION_ID, DIAGNOSIS_ID));

    assertThat(consultation.getDiagnoses()).hasSize(2);
    assertThat(consultation.getDiagnoses().get(0)).isSameAs(previous);
    assertThat(consultation.getDiagnoses().get(1)).isSameAs(reevaluation);
    // the previous diagnosis is only marked as superseded
    assertThat(previous.getStatus()).isEqualTo(DiagnosisStatus.SUPERSEDED);
    assertThat(previous.isCurrent()).isFalse();
    assertThat(previous.getDescription()).isEqualTo("Acute gastroenteritis");
    assertThat(previous.getType()).isEqualTo(DiagnosisType.PRESUMPTIVE);
    assertThat(previous.getRationale()).isEqualTo("Vomiting and abdominal pain after a food change");
    assertThat(previous.getDifferentialDiagnoses()).containsExactly("Foreign body", "Pancreatitis");
    // the new diagnosis points to the previous one and takes its place as primary
    assertThat(reevaluation.getDescription()).isEqualTo("Pancreatitis");
    assertThat(reevaluation.getType()).isEqualTo(DiagnosisType.DEFINITIVE);
    assertThat(reevaluation.getStatus()).isEqualTo(DiagnosisStatus.CURRENT);
    assertThat(reevaluation.isReevaluation()).isTrue();
    assertThat(reevaluation.getPreviousDiagnosisId()).isEqualTo(DIAGNOSIS_ID);
    assertThat(reevaluation.isPrimary()).isTrue();
  }

  @Test
  @DisplayName("Reevaluating a secondary diagnosis gives a secondary diagnosis")
  void reevaluationKeepsImportance() {
    var consultation = inProgressConsultation();
    consultation.registerDiagnosis(primaryDiagnosisCommand(CONSULTATION_ID));
    withId(consultation.registerDiagnosis(secondaryDiagnosisCommand(CONSULTATION_ID)), DIAGNOSIS_ID);

    var reevaluation = consultation.reevaluateDiagnosis(
        reevaluationCommand(CONSULTATION_ID, DIAGNOSIS_ID));

    assertThat(reevaluation.isPrimary()).isFalse();
  }

  @Test
  @DisplayName("After a reevaluation the consultation still has exactly one current primary diagnosis")
  void reevaluationReplacesPrimary() {
    var consultation = inProgressConsultation();
    withId(consultation.registerDiagnosis(primaryDiagnosisCommand(CONSULTATION_ID)), DIAGNOSIS_ID);
    consultation.reevaluateDiagnosis(reevaluationCommand(CONSULTATION_ID, DIAGNOSIS_ID));

    assertThatThrownBy(() -> consultation.registerDiagnosis(primaryDiagnosisCommand(CONSULTATION_ID)))
        .isInstanceOf(InvalidConsultationStateException.class);
    assertThat(consultation.pendingItems()).containsExactly("anamnesis", "physiologicalAssessment");
  }

  @Test
  @DisplayName("A diagnosis can be reevaluated only once")
  void cannotReevaluateTwice() {
    var consultation = inProgressConsultation();
    withId(consultation.registerDiagnosis(primaryDiagnosisCommand(CONSULTATION_ID)), DIAGNOSIS_ID);
    consultation.reevaluateDiagnosis(reevaluationCommand(CONSULTATION_ID, DIAGNOSIS_ID));

    assertThatThrownBy(() -> consultation.reevaluateDiagnosis(
        reevaluationCommand(CONSULTATION_ID, DIAGNOSIS_ID)))
        .isInstanceOf(InvalidConsultationStateException.class);
    assertThat(consultation.getDiagnoses()).hasSize(2);
  }

  @Test
  @DisplayName("A diagnosis of another consultation cannot be reevaluated")
  void cannotReevaluateUnknownDiagnosis() {
    var consultation = inProgressConsultation();
    consultation.registerDiagnosis(primaryDiagnosisCommand(CONSULTATION_ID));

    assertThatThrownBy(() -> consultation.reevaluateDiagnosis(reevaluationCommand(CONSULTATION_ID, 999L)))
        .isInstanceOf(IllegalArgumentException.class);
    assertThat(consultation.getDiagnoses()).hasSize(1);
  }

  // ------------------------------------------------------------- completion

  @Test
  @DisplayName("Completing without data reports everything that is missing and changes nothing")
  void completeReportsPendingItems() {
    var consultation = inProgressConsultation();

    assertThatThrownBy(consultation::complete)
        .isInstanceOfSatisfying(IncompleteConsultationException.class, exception ->
            assertThat(exception.getPendingItems())
                .containsExactly("anamnesis", "physiologicalAssessment", "primaryDiagnosis"));
    assertThat(consultation.getStatus()).isEqualTo(ConsultationStatus.IN_PROGRESS);
    assertThat(consultation.getCompletedAt()).isNull();
    assertThat(domainEvents(consultation)).isEmpty();
  }

  @Test
  @DisplayName("Completing without a primary diagnosis reports only the diagnosis")
  void completeNeedsPrimaryDiagnosis() {
    var consultation = inProgressConsultation();
    consultation.registerAnamnesis(anamnesisCommand(CONSULTATION_ID));
    consultation.registerPhysiologicalAssessment(assessmentCommand(CONSULTATION_ID));
    consultation.registerDiagnosis(secondaryDiagnosisCommand(CONSULTATION_ID));

    assertThatThrownBy(consultation::complete)
        .isInstanceOfSatisfying(IncompleteConsultationException.class, exception ->
            assertThat(exception.getPendingItems()).containsExactly("primaryDiagnosis"));
    assertThat(consultation.getStatus()).isEqualTo(ConsultationStatus.IN_PROGRESS);
  }

  @Test
  @DisplayName("Completing without anamnesis reports only the anamnesis")
  void completeNeedsAnamnesis() {
    var consultation = inProgressConsultation();
    consultation.registerPhysiologicalAssessment(assessmentCommand(CONSULTATION_ID));
    consultation.registerDiagnosis(primaryDiagnosisCommand(CONSULTATION_ID));

    assertThatThrownBy(consultation::complete)
        .isInstanceOfSatisfying(IncompleteConsultationException.class, exception ->
            assertThat(exception.getPendingItems()).containsExactly("anamnesis"));
  }

  @Test
  @DisplayName("Completing without a physiological assessment reports only the assessment")
  void completeNeedsAssessment() {
    var consultation = inProgressConsultation();
    consultation.registerAnamnesis(anamnesisCommand(CONSULTATION_ID));
    consultation.registerDiagnosis(primaryDiagnosisCommand(CONSULTATION_ID));

    assertThatThrownBy(consultation::complete)
        .isInstanceOfSatisfying(IncompleteConsultationException.class, exception ->
            assertThat(exception.getPendingItems()).containsExactly("physiologicalAssessment"));
  }

  @Test
  @DisplayName("A complete consultation is COMPLETED and registers the completed event")
  void completesConsultation() {
    var consultation = completableConsultation();
    assertThat(consultation.pendingItems()).isEmpty();

    consultation.complete();

    assertThat(consultation.getStatus()).isEqualTo(ConsultationStatus.COMPLETED);
    assertThat(consultation.getCompletedAt()).isNotNull();
    var events = domainEvents(consultation, ClinicalConsultationCompletedEvent.class);
    assertThat(events).hasSize(1);
    assertThat(events.get(0).consultationId()).isEqualTo(CONSULTATION_ID);
    assertThat(events.get(0).petId()).isEqualTo(5L);
    assertThat(events.get(0).veterinarianId()).isEqualTo(100L);
    assertThat(events.get(0).primaryDiagnosis()).isEqualTo("Acute gastroenteritis");
    assertThat(events.get(0).completedAt()).isEqualTo(consultation.getCompletedAt());
  }

  @Test
  @DisplayName("The completed event carries the reevaluated primary diagnosis")
  void completedEventUsesCurrentPrimaryDiagnosis() {
    var consultation = completableConsultation();
    var primary = consultation.getDiagnoses().get(0);
    withId(primary, DIAGNOSIS_ID);
    consultation.reevaluateDiagnosis(reevaluationCommand(CONSULTATION_ID, DIAGNOSIS_ID));

    consultation.complete();

    var events = domainEvents(consultation, ClinicalConsultationCompletedEvent.class);
    assertThat(events.get(0).primaryDiagnosis()).isEqualTo("Pancreatitis");
  }

  @Test
  @DisplayName("A completed consultation is closed: nothing can be added, completed or cancelled")
  void completedConsultationIsClosed() {
    var consultation = completableConsultation();
    consultation.complete();

    assertThatThrownBy(consultation::start).isInstanceOf(InvalidConsultationStateException.class);
    assertThatThrownBy(consultation::complete).isInstanceOf(InvalidConsultationStateException.class);
    assertThatThrownBy(() -> consultation.cancel("Mistake"))
        .isInstanceOf(InvalidConsultationStateException.class);
    assertThatThrownBy(() -> consultation.registerPhysiologicalAssessment(assessmentCommand(CONSULTATION_ID)))
        .isInstanceOf(InvalidConsultationStateException.class);
    assertThatThrownBy(() -> consultation.addClinicalFinding(findingCommand(CONSULTATION_ID)))
        .isInstanceOf(InvalidConsultationStateException.class);
    assertThatThrownBy(() -> consultation.registerDiagnosis(secondaryDiagnosisCommand(CONSULTATION_ID)))
        .isInstanceOf(InvalidConsultationStateException.class);
    assertThatThrownBy(() -> consultation.reevaluateDiagnosis(reevaluationCommand(CONSULTATION_ID, 1L)))
        .isInstanceOf(InvalidConsultationStateException.class);
  }

  @Test
  @DisplayName("A consultation that is not in progress cannot be completed")
  void createdConsultationCannotBeCompleted() {
    var consultation = newConsultation();

    assertThatThrownBy(consultation::complete)
        .isInstanceOf(InvalidConsultationStateException.class);
  }

  // ------------------------------------------------------------ cancellation

  @Test
  @DisplayName("A CREATED consultation can be cancelled with a reason")
  void cancelsCreatedConsultation() {
    var consultation = newConsultation();

    consultation.cancel("The owner did not come");

    assertThat(consultation.getStatus()).isEqualTo(ConsultationStatus.CANCELLED);
    assertThat(consultation.getCancellation().reason()).isEqualTo("The owner did not come");
    assertThat(consultation.getCancellation().cancelledAt()).isNotNull();
    var events = domainEvents(consultation, ClinicalConsultationCancelledEvent.class);
    assertThat(events).hasSize(1);
    assertThat(events.get(0).consultationId()).isEqualTo(CONSULTATION_ID);
    assertThat(events.get(0).reason()).isEqualTo("The owner did not come");
    assertThat(events.get(0).cancelledAt()).isEqualTo(consultation.getCancellation().cancelledAt());
  }

  @Test
  @DisplayName("An IN_PROGRESS consultation can be cancelled and keeps its records")
  void cancelsInProgressConsultation() {
    var consultation = inProgressConsultation();
    consultation.registerAnamnesis(anamnesisCommand(CONSULTATION_ID));

    consultation.cancel("Emergency surgery");

    assertThat(consultation.getStatus()).isEqualTo(ConsultationStatus.CANCELLED);
    assertThat(consultation.getAnamnesis()).isNotNull();
  }

  @Test
  @DisplayName("The cancellation reason is required")
  void cancellationNeedsReason() {
    var consultation = newConsultation();

    assertThatThrownBy(() -> consultation.cancel(" "))
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> consultation.cancel(null))
        .isInstanceOf(IllegalArgumentException.class);
    assertThat(consultation.getStatus()).isEqualTo(ConsultationStatus.CREATED);
  }

  @Test
  @DisplayName("A cancelled consultation is closed")
  void cancelledConsultationIsClosed() {
    var consultation = newConsultation();
    consultation.cancel("The owner did not come");
    clearDomainEvents(consultation);

    assertThatThrownBy(() -> consultation.cancel("Again"))
        .isInstanceOf(InvalidConsultationStateException.class);
    assertThatThrownBy(consultation::start).isInstanceOf(InvalidConsultationStateException.class);
    assertThatThrownBy(consultation::complete).isInstanceOf(InvalidConsultationStateException.class);
    assertThat(domainEvents(consultation)).isEmpty();
  }

  // ------------------------------------------------------------------ others

  @Test
  @DisplayName("Only the veterinarian in charge attends the consultation")
  void isAttendedBy() {
    var consultation = newConsultation();

    assertThat(consultation.isAttendedBy(new VeterinarianId(100L))).isTrue();
    assertThat(consultation.isAttendedBy(new VeterinarianId(101L))).isFalse();
  }

  @Test
  @DisplayName("The lists returned by the aggregate cannot be used to change its records")
  void listsAreReadOnly() {
    var consultation = completableConsultation();

    assertThatThrownBy(() -> consultation.getDiagnoses().clear())
        .isInstanceOf(UnsupportedOperationException.class);
    assertThatThrownBy(() -> consultation.getPhysiologicalAssessments().clear())
        .isInstanceOf(UnsupportedOperationException.class);
    assertThatThrownBy(() -> consultation.getClinicalFindings().add(null))
        .isInstanceOf(UnsupportedOperationException.class);
    assertThat(consultation.getDiagnoses()).hasSize(1);
  }
}

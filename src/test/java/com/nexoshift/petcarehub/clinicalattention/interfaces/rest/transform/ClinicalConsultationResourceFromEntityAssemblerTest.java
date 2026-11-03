package com.nexoshift.petcarehub.clinicalattention.interfaces.rest.transform;

import static com.nexoshift.petcarehub.clinicalattention.support.ClinicalFixtures.CONSULTATION_ID;
import static com.nexoshift.petcarehub.clinicalattention.support.ClinicalFixtures.anamnesisCommand;
import static com.nexoshift.petcarehub.clinicalattention.support.ClinicalFixtures.assessmentCommand;
import static com.nexoshift.petcarehub.clinicalattention.support.ClinicalFixtures.findingCommand;
import static com.nexoshift.petcarehub.clinicalattention.support.ClinicalFixtures.inProgressConsultation;
import static com.nexoshift.petcarehub.clinicalattention.support.ClinicalFixtures.newConsultation;
import static com.nexoshift.petcarehub.clinicalattention.support.ClinicalFixtures.primaryDiagnosisCommand;
import static com.nexoshift.petcarehub.clinicalattention.support.ClinicalFixtures.reevaluationCommand;
import static com.nexoshift.petcarehub.clinicalattention.support.ClinicalTestSupport.withId;
import static org.assertj.core.api.Assertions.assertThat;

import com.nexoshift.petcarehub.clinicalattention.domain.model.aggregates.ClinicalConsultation;
import com.nexoshift.petcarehub.clinicalattention.domain.model.commands.CreateClinicalConsultationCommand;
import com.nexoshift.petcarehub.clinicalattention.domain.model.commands.RegisterPhysiologicalAssessmentCommand;
import com.nexoshift.petcarehub.clinicalattention.domain.model.valueobjects.BodySystem;
import com.nexoshift.petcarehub.clinicalattention.domain.model.valueobjects.ConsultationStatus;
import com.nexoshift.petcarehub.clinicalattention.domain.model.valueobjects.DiagnosisStatus;
import com.nexoshift.petcarehub.clinicalattention.domain.model.valueobjects.DiagnosisType;
import com.nexoshift.petcarehub.clinicalattention.domain.model.valueobjects.FindingSeverity;
import com.nexoshift.petcarehub.clinicalattention.domain.model.valueobjects.HydrationStatus;
import com.nexoshift.petcarehub.clinicalattention.domain.model.valueobjects.VeterinarianId;
import com.nexoshift.petcarehub.clinicalattention.interfaces.rest.resources.AddClinicalFindingResource;
import com.nexoshift.petcarehub.clinicalattention.interfaces.rest.resources.CreateClinicalConsultationResource;
import com.nexoshift.petcarehub.clinicalattention.interfaces.rest.resources.ReevaluateDiagnosisResource;
import com.nexoshift.petcarehub.clinicalattention.interfaces.rest.resources.RegisterAnamnesisResource;
import com.nexoshift.petcarehub.clinicalattention.interfaces.rest.resources.RegisterDiagnosisResource;
import com.nexoshift.petcarehub.clinicalattention.interfaces.rest.resources.RegisterPhysiologicalAssessmentResource;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ClinicalConsultationResourceFromEntityAssemblerTest {

  @Test
  @DisplayName("A new consultation is shown without records")
  void mapsNewConsultation() {
    var resource = ClinicalConsultationResourceFromEntityAssembler
        .toResourceFromEntity(newConsultation());

    assertThat(resource.id()).isEqualTo(CONSULTATION_ID);
    assertThat(resource.petId()).isEqualTo(5L);
    assertThat(resource.veterinarianId()).isEqualTo(100L);
    assertThat(resource.clinicId()).isEqualTo(3L);
    assertThat(resource.consultingRoomId()).isEqualTo(7L);
    assertThat(resource.reason()).isEqualTo("Vomits and does not eat since yesterday");
    assertThat(resource.status()).isEqualTo(ConsultationStatus.CREATED);
    assertThat(resource.startedAt()).isNull();
    assertThat(resource.cancellationReason()).isNull();
    assertThat(resource.cancelledAt()).isNull();
    assertThat(resource.anamnesis()).isNull();
    assertThat(resource.physiologicalAssessments()).isEmpty();
    assertThat(resource.findings()).isEmpty();
    assertThat(resource.diagnoses()).isEmpty();
  }

  @Test
  @DisplayName("A consultation without consulting room is shown with a null room")
  void mapsConsultationWithoutRoom() {
    var command = new CreateClinicalConsultationCommand(5L, 10L, 3L, null, "Check-up");
    var consultation = withId(new ClinicalConsultation(command, new VeterinarianId(100L)), 2L);

    var resource = ClinicalConsultationResourceFromEntityAssembler.toResourceFromEntity(consultation);

    assertThat(resource.consultingRoomId()).isNull();
  }

  @Test
  @DisplayName("All the records of the consultation are mapped")
  void mapsRecords() {
    var consultation = inProgressConsultation();
    withId(consultation.registerAnamnesis(anamnesisCommand(CONSULTATION_ID)), 11L);
    withId(consultation.registerPhysiologicalAssessment(assessmentCommand(CONSULTATION_ID)), 12L);
    withId(consultation.addClinicalFinding(findingCommand(CONSULTATION_ID)), 13L);
    withId(consultation.registerDiagnosis(primaryDiagnosisCommand(CONSULTATION_ID)), 14L);

    var resource = ClinicalConsultationResourceFromEntityAssembler.toResourceFromEntity(consultation);

    assertThat(resource.status()).isEqualTo(ConsultationStatus.IN_PROGRESS);
    assertThat(resource.startedAt()).isNotNull();

    var anamnesis = resource.anamnesis();
    assertThat(anamnesis.id()).isEqualTo(11L);
    assertThat(anamnesis.chiefComplaint()).isEqualTo("Vomiting");
    assertThat(anamnesis.evolutionTime()).isEqualTo("2 days");
    assertThat(anamnesis.symptoms()).containsExactly("Vomiting", "Lethargy");
    assertThat(anamnesis.diet()).isEqualTo("Dry food");
    assertThat(anamnesis.behavior()).isEqualTo("Calm");
    assertThat(anamnesis.previousTreatments()).isEqualTo("Deworming last month");
    assertThat(anamnesis.ownerObservations()).isEqualTo("The owner changed the food last week");

    assertThat(resource.physiologicalAssessments()).hasSize(1);
    var assessment = resource.physiologicalAssessments().get(0);
    assertThat(assessment.id()).isEqualTo(12L);
    assertThat(assessment.weightKg()).isEqualByComparingTo("12.50");
    assertThat(assessment.temperatureCelsius()).isEqualByComparingTo("38.6");
    assertThat(assessment.heartRateBpm()).isEqualTo(110);
    assertThat(assessment.respiratoryRateBpm()).isEqualTo(28);
    assertThat(assessment.bodyConditionScore()).isEqualTo(5);
    assertThat(assessment.hydrationStatus()).isEqualTo(HydrationStatus.MILD_DEHYDRATION);
    assertThat(assessment.mucousMembranes()).isEqualTo("Pale pink");
    assertThat(assessment.capillaryRefillTimeSeconds()).isEqualByComparingTo("2.0");
    assertThat(assessment.observations()).isEqualTo("Quiet but alert");
    assertThat(assessment.recordedAt()).isNotNull();

    assertThat(resource.findings()).hasSize(1);
    var finding = resource.findings().get(0);
    assertThat(finding.id()).isEqualTo(13L);
    assertThat(finding.bodySystem()).isEqualTo(BodySystem.DIGESTIVE);
    assertThat(finding.description()).isEqualTo("Painful abdomen on palpation");
    assertThat(finding.severity()).isEqualTo(FindingSeverity.MODERATE);

    assertThat(resource.diagnoses()).hasSize(1);
    var diagnosis = resource.diagnoses().get(0);
    assertThat(diagnosis.id()).isEqualTo(14L);
    assertThat(diagnosis.description()).isEqualTo("Acute gastroenteritis");
    assertThat(diagnosis.type()).isEqualTo(DiagnosisType.PRESUMPTIVE);
    assertThat(diagnosis.primary()).isTrue();
    assertThat(diagnosis.status()).isEqualTo(DiagnosisStatus.CURRENT);
    assertThat(diagnosis.previousDiagnosisId()).isNull();
    assertThat(diagnosis.differentialDiagnoses()).containsExactly("Foreign body", "Pancreatitis");
    assertThat(diagnosis.diagnosedByVeterinarianId()).isEqualTo(100L);
  }

  @Test
  @DisplayName("Optional measurements that were not taken are shown as null")
  void mapsAssessmentWithoutOptionalData() {
    var consultation = inProgressConsultation();
    var command = new RegisterPhysiologicalAssessmentCommand(CONSULTATION_ID, 10L,
        new BigDecimal("8"), new BigDecimal("38.2"), 90, 22, null, null, null, null, null);
    consultation.registerPhysiologicalAssessment(command);

    var assessment = ClinicalConsultationResourceFromEntityAssembler
        .toResourceFromEntity(consultation).physiologicalAssessments().get(0);

    assertThat(assessment.bodyConditionScore()).isNull();
    assertThat(assessment.hydrationStatus()).isNull();
    assertThat(assessment.mucousMembranes()).isNull();
    assertThat(assessment.capillaryRefillTimeSeconds()).isNull();
    assertThat(assessment.observations()).isNull();
  }

  @Test
  @DisplayName("A reevaluation is shown next to the superseded diagnosis")
  void mapsReevaluation() {
    var consultation = inProgressConsultation();
    withId(consultation.registerDiagnosis(primaryDiagnosisCommand(CONSULTATION_ID)), 14L);
    withId(consultation.reevaluateDiagnosis(reevaluationCommand(CONSULTATION_ID, 14L)), 15L);

    var diagnoses = ClinicalConsultationResourceFromEntityAssembler
        .toResourceFromEntity(consultation).diagnoses();

    assertThat(diagnoses).hasSize(2);
    assertThat(diagnoses.get(0).id()).isEqualTo(14L);
    assertThat(diagnoses.get(0).status()).isEqualTo(DiagnosisStatus.SUPERSEDED);
    assertThat(diagnoses.get(0).description()).isEqualTo("Acute gastroenteritis");
    assertThat(diagnoses.get(1).id()).isEqualTo(15L);
    assertThat(diagnoses.get(1).status()).isEqualTo(DiagnosisStatus.CURRENT);
    assertThat(diagnoses.get(1).previousDiagnosisId()).isEqualTo(14L);
    assertThat(diagnoses.get(1).primary()).isTrue();
  }

  @Test
  @DisplayName("A cancelled consultation shows the reason and the date")
  void mapsCancellation() {
    var consultation = newConsultation();
    consultation.cancel("The owner did not come");

    var resource = ClinicalConsultationResourceFromEntityAssembler.toResourceFromEntity(consultation);

    assertThat(resource.status()).isEqualTo(ConsultationStatus.CANCELLED);
    assertThat(resource.cancellationReason()).isEqualTo("The owner did not come");
    assertThat(resource.cancelledAt()).isNotNull();
  }

  @Test
  @DisplayName("Resources are converted to commands with the ids of the path and the user")
  void mapsCommands() {
    var create = CreateClinicalConsultationCommandFromResourceAssembler.toCommandFromResource(
        10L, new CreateClinicalConsultationResource(5L, 3L, 7L, " Check-up "));
    assertThat(create.petId()).isEqualTo(5L);
    assertThat(create.veterinarianUserId()).isEqualTo(10L);
    assertThat(create.clinicId()).isEqualTo(3L);
    assertThat(create.consultingRoomId()).isEqualTo(7L);
    assertThat(create.reason()).isEqualTo("Check-up");

    var anamnesis = RegisterAnamnesisCommandFromResourceAssembler.toCommandFromResource(1L, 10L,
        new RegisterAnamnesisResource("Vomiting", "2 days", List.of("Vomiting"), null, null, null, null));
    assertThat(anamnesis.consultationId()).isEqualTo(1L);
    assertThat(anamnesis.requesterUserId()).isEqualTo(10L);
    assertThat(anamnesis.symptoms()).containsExactly("Vomiting");

    var assessment = RegisterPhysiologicalAssessmentCommandFromResourceAssembler.toCommandFromResource(
        1L, 10L, new RegisterPhysiologicalAssessmentResource(new BigDecimal("12"),
            new BigDecimal("38.5"), 100, 20, 6, HydrationStatus.NORMAL, "Pink", new BigDecimal("1.5"),
            "Calm"));
    assertThat(assessment.consultationId()).isEqualTo(1L);
    assertThat(assessment.heartRateBpm()).isEqualTo(100);
    assertThat(assessment.bodyConditionScore()).isEqualTo(6);
    assertThat(assessment.hydrationStatus()).isEqualTo(HydrationStatus.NORMAL);

    var finding = AddClinicalFindingCommandFromResourceAssembler.toCommandFromResource(
        1L, 10L, new AddClinicalFindingResource(BodySystem.OCULAR, "Red eye", FindingSeverity.MILD));
    assertThat(finding.bodySystem()).isEqualTo(BodySystem.OCULAR);
    assertThat(finding.severity()).isEqualTo(FindingSeverity.MILD);

    var diagnosis = RegisterDiagnosisCommandFromResourceAssembler.toCommandFromResource(1L, 10L,
        new RegisterDiagnosisResource("Gastritis", DiagnosisType.DEFINITIVE, true, List.of(), null));
    assertThat(diagnosis.primary()).isTrue();
    assertThat(diagnosis.type()).isEqualTo(DiagnosisType.DEFINITIVE);

    var reevaluation = ReevaluateDiagnosisCommandFromResourceAssembler.toCommandFromResource(
        1L, 14L, 10L,
        new ReevaluateDiagnosisResource("Pancreatitis", DiagnosisType.DEFINITIVE, List.of("A"), "Lab"));
    assertThat(reevaluation.consultationId()).isEqualTo(1L);
    assertThat(reevaluation.previousDiagnosisId()).isEqualTo(14L);
    assertThat(reevaluation.requesterUserId()).isEqualTo(10L);
    assertThat(reevaluation.differentialDiagnoses()).containsExactly("A");
  }
}

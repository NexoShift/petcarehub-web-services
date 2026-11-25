package com.nexoshift.petcarehub.clinicalattention.domain;

import com.nexoshift.petcarehub.clinicalattention.domain.exceptions.InvalidConsultationStateException;
import com.nexoshift.petcarehub.clinicalattention.domain.model.commands.RegisterPrescriptionCommand;
import com.nexoshift.petcarehub.clinicalattention.domain.model.commands.RegisterPrescriptionCommand.MedicationItem;
import com.nexoshift.petcarehub.clinicalattention.domain.model.commands.RegisterTreatmentCommand;
import com.nexoshift.petcarehub.clinicalattention.domain.model.valueobjects.TreatmentStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import static com.nexoshift.petcarehub.clinicalattention.support.ClinicalFixtures.CONSULTATION_ID;
import static com.nexoshift.petcarehub.clinicalattention.support.ClinicalFixtures.USER_ID;
import static com.nexoshift.petcarehub.clinicalattention.support.ClinicalFixtures.inProgressConsultation;
import static com.nexoshift.petcarehub.clinicalattention.support.ClinicalFixtures.newConsultation;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TreatmentAndPrescriptionTest {

  private static final MedicationItem AMOXICILLIN =
      new MedicationItem(" Amoxicillin ", "250 mg", "Every 12 hours", "10 days", " ");

  private static RegisterTreatmentCommand treatment(LocalDate start, LocalDate end) {
    return new RegisterTreatmentCommand(CONSULTATION_ID, USER_ID, "Diet", "Rice", start, end);
  }

  @Test
  @DisplayName("A treatment starts ACTIVE and is signed by the vet in charge")
  void treatmentIsActive() {
    var consultation = inProgressConsultation();

    var treatment = consultation.registerTreatment(treatment(LocalDate.now(), null));

    assertThat(treatment.getTreatmentStatus()).isEqualTo(TreatmentStatus.ACTIVE);
    assertThat(treatment.getPrescribedBy()).isEqualTo(consultation.getVeterinarianId());
    assertThat(consultation.getTreatments()).hasSize(1);
  }

  @Test
  @DisplayName("A treatment cannot end before it starts")
  void treatmentDates() {
    var today = LocalDate.now();

    assertThatThrownBy(() -> treatment(today, today.minusDays(1)))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  @DisplayName("A prescription keeps its medications trimmed and the issue date is set by the server")
  void prescriptionIsIssuedNow() {
    var consultation = inProgressConsultation();
    var before = Instant.now();

    var prescription = consultation.registerPrescription(
        new RegisterPrescriptionCommand(CONSULTATION_ID, USER_ID, null, List.of(AMOXICILLIN)));

    assertThat(prescription.getIssuedAt()).isBetween(before, Instant.now());
    assertThat(prescription.getMedications()).singleElement()
        .satisfies(medication -> {
          assertThat(medication.getName()).isEqualTo("Amoxicillin");
          assertThat(medication.getIndication()).isNull();
        });
  }

  @Test
  @DisplayName("A prescription needs at least one medication")
  void prescriptionNeedsMedication() {
    assertThatThrownBy(() -> new RegisterPrescriptionCommand(CONSULTATION_ID, USER_ID, "x",
        List.of())).isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> new RegisterPrescriptionCommand(CONSULTATION_ID, USER_ID, "x",
        null)).isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  @DisplayName("A medication needs name, dosage, frequency and duration")
  void medicationRequiredFields() {
    assertThatThrownBy(() -> new MedicationItem("Amoxicillin", " ", "Every 12 hours", "10 days",
        null)).isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  @DisplayName("Treatments and prescriptions are rejected before the consultation starts")
  void needsInProgress() {
    var consultation = newConsultation();

    assertThatThrownBy(() -> consultation.registerTreatment(treatment(LocalDate.now(), null)))
        .isInstanceOf(InvalidConsultationStateException.class);
    assertThatThrownBy(() -> consultation.registerPrescription(new RegisterPrescriptionCommand(
        CONSULTATION_ID, USER_ID, null, List.of(AMOXICILLIN))))
        .isInstanceOf(InvalidConsultationStateException.class);
  }
}

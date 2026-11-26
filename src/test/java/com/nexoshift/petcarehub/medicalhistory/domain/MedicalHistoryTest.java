package com.nexoshift.petcarehub.medicalhistory.domain;

import com.nexoshift.petcarehub.medicalhistory.domain.model.aggregates.MedicalHistory;
import com.nexoshift.petcarehub.medicalhistory.domain.model.commands.RecordAllergyCommand;
import com.nexoshift.petcarehub.medicalhistory.domain.model.commands.RecordConsultationCompletedCommand;
import com.nexoshift.petcarehub.medicalhistory.domain.model.commands.RecordVaccinationCommand;
import com.nexoshift.petcarehub.medicalhistory.domain.model.valueobjects.AllergySeverity;
import com.nexoshift.petcarehub.medicalhistory.domain.model.valueobjects.ReporterType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MedicalHistoryTest {

  private static RecordConsultationCompletedCommand completed(long consultationId, Instant at) {
    return new RecordConsultationCompletedCommand(consultationId, 5L, 3L, 100L, "Otitis", at);
  }

  private static RecordVaccinationCommand vaccine(LocalDate appliedOn, LocalDate next) {
    return new RecordVaccinationCommand(5L, 10L, "Rabies", 1, appliedOn, next, null, null);
  }

  @Test
  @DisplayName("A repeated consultation event does not duplicate the entry")
  void idempotentEntries() {
    var history = new MedicalHistory(5L);
    var now = Instant.now();

    history.recordConsultationCompleted(completed(1L, now));
    history.recordConsultationCompleted(completed(1L, now));

    assertThat(history.getEntries()).hasSize(1);
  }

  @Test
  @DisplayName("The timeline comes most recent first")
  void timelineOrder() {
    var history = new MedicalHistory(5L);
    var now = Instant.now();

    history.recordConsultationCompleted(completed(1L, now.minus(10, ChronoUnit.DAYS)));
    history.recordConsultationCompleted(completed(2L, now));

    assertThat(history.getEntries()).extracting(entry -> entry.getReferenceId())
        .containsExactly(2L, 1L);
  }

  @Test
  @DisplayName("An allergy keeps who reported it; texts are trimmed")
  void allergy() {
    var history = new MedicalHistory(5L);

    var allergy = history.recordAllergy(new RecordAllergyCommand(5L, 10L, "  Chicken ", " ",
        AllergySeverity.MILD, null), ReporterType.OWNER);

    assertThat(allergy.getAllergen()).isEqualTo("Chicken");
    assertThat(allergy.getReaction()).isNull();
    assertThat(allergy.getReportedBy()).isEqualTo(ReporterType.OWNER);
  }

  @Test
  @DisplayName("A vaccination cannot be in the future and the booster must come after it")
  void vaccinationDates() {
    var history = new MedicalHistory(5L);
    var today = LocalDate.now();

    assertThatThrownBy(() -> history.recordVaccination(vaccine(today.plusDays(1), null), 100L, 3L))
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> history.recordVaccination(vaccine(today, today), 100L, 3L))
        .isInstanceOf(IllegalArgumentException.class);
    history.recordVaccination(vaccine(today, today.plusYears(1)), 100L, 3L);
    assertThat(history.getVaccinations()).hasSize(1);
  }

  @Test
  @DisplayName("Allergen and severity are required")
  void allergyRequired() {
    assertThatThrownBy(() -> new RecordAllergyCommand(5L, 10L, " ", null, AllergySeverity.MILD,
        null)).isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> new RecordAllergyCommand(5L, 10L, "Chicken", null, null, null))
        .isInstanceOf(IllegalArgumentException.class);
  }
}

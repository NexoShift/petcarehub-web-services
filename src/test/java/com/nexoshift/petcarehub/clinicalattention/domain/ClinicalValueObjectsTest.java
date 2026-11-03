package com.nexoshift.petcarehub.clinicalattention.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.nexoshift.petcarehub.clinicalattention.domain.model.valueobjects.BodyConditionScore;
import com.nexoshift.petcarehub.clinicalattention.domain.model.valueobjects.CancellationDetail;
import com.nexoshift.petcarehub.clinicalattention.domain.model.valueobjects.ClinicId;
import com.nexoshift.petcarehub.clinicalattention.domain.model.valueobjects.ClinicalConsultationId;
import com.nexoshift.petcarehub.clinicalattention.domain.model.valueobjects.ConsultationStatus;
import com.nexoshift.petcarehub.clinicalattention.domain.model.valueobjects.ConsultingRoomId;
import com.nexoshift.petcarehub.clinicalattention.domain.model.valueobjects.PetId;
import com.nexoshift.petcarehub.clinicalattention.domain.model.valueobjects.VeterinarianId;
import com.nexoshift.petcarehub.clinicalattention.domain.model.valueobjects.VitalSigns;
import java.math.BigDecimal;
import java.time.Instant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ClinicalValueObjectsTest {

  private static VitalSigns vitals(String weight, String temperature, int heart, int respiratory) {
    return new VitalSigns(new BigDecimal(weight), new BigDecimal(temperature), heart, respiratory);
  }

  // -------------------------------------------------------------- identifiers

  @Test
  @DisplayName("Ids of other contexts are positive numbers")
  void idsMustBePositive() {
    assertThat(new PetId(1L).petId()).isEqualTo(1L);
    assertThat(new VeterinarianId(2L).veterinarianId()).isEqualTo(2L);
    assertThat(new ClinicId(3L).clinicId()).isEqualTo(3L);
    assertThat(new ConsultingRoomId(4L).consultingRoomId()).isEqualTo(4L);
    assertThat(new ClinicalConsultationId(5L).clinicalConsultationId()).isEqualTo(5L);

    assertThatThrownBy(() -> new PetId(null)).isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> new PetId(0L)).isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> new VeterinarianId(-1L)).isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> new ClinicId(0L)).isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> new ConsultingRoomId(null)).isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> new ClinicalConsultationId(0L))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  @DisplayName("Ids are compared by value")
  void idsAreComparedByValue() {
    assertThat(new PetId(1L)).isEqualTo(new PetId(1L));
    assertThat(new PetId(1L)).isNotEqualTo(new PetId(2L));
    assertThat(new VeterinarianId(7L)).isEqualTo(new VeterinarianId(7L));
  }

  // -------------------------------------------------------------- vital signs

  @Test
  @DisplayName("Vital signs inside the allowed ranges are accepted")
  void vitalSignsAccepted() {
    var limits = vitals("500", "30", 600, 300);
    var small = vitals("0.01", "45", 1, 1);

    assertThat(limits.weightKg()).isEqualByComparingTo("500");
    assertThat(small.temperatureCelsius()).isEqualByComparingTo("45");
  }

  @Test
  @DisplayName("Weight must be greater than 0 and at most 500 kg")
  void weightRange() {
    assertThatThrownBy(() -> vitals("0", "38", 100, 20)).isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> vitals("-1", "38", 100, 20)).isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> vitals("500.01", "38", 100, 20))
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> new VitalSigns(null, new BigDecimal("38"), 100, 20))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  @DisplayName("Temperature must be between 30 and 45 degrees Celsius")
  void temperatureRange() {
    assertThatThrownBy(() -> vitals("10", "29.9", 100, 20))
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> vitals("10", "45.1", 100, 20))
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> new VitalSigns(new BigDecimal("10"), null, 100, 20))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  @DisplayName("Heart rate and respiratory rate must be positive and plausible")
  void ratesRange() {
    assertThatThrownBy(() -> vitals("10", "38", 0, 20)).isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> vitals("10", "38", 601, 20)).isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> vitals("10", "38", 100, 0)).isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> vitals("10", "38", 100, 301))
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> new VitalSigns(new BigDecimal("10"), new BigDecimal("38"), null, 20))
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> new VitalSigns(new BigDecimal("10"), new BigDecimal("38"), 100, null))
        .isInstanceOf(IllegalArgumentException.class);
  }

  // ---------------------------------------------------------------- body score

  @Test
  @DisplayName("Body condition score goes from 1 to 9")
  void bodyConditionScoreRange() {
    assertThat(new BodyConditionScore(1).score()).isEqualTo(1);
    assertThat(new BodyConditionScore(9).score()).isEqualTo(9);
    assertThatThrownBy(() -> new BodyConditionScore(0)).isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> new BodyConditionScore(10)).isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> new BodyConditionScore(null))
        .isInstanceOf(IllegalArgumentException.class);
  }

  // ------------------------------------------------------------- cancellation

  @Test
  @DisplayName("A cancellation detail needs a reason and a date")
  void cancellationDetail() {
    var now = Instant.now();

    assertThat(new CancellationDetail("No show", now).reason()).isEqualTo("No show");
    assertThatThrownBy(() -> new CancellationDetail(" ", now))
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> new CancellationDetail(null, now))
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> new CancellationDetail("x".repeat(501), now))
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> new CancellationDetail("No show", null))
        .isInstanceOf(IllegalArgumentException.class);
  }

  // ------------------------------------------------------------------- status

  @Test
  @DisplayName("Only CREATED and IN_PROGRESS consultations can be cancelled")
  void statusAllowsCancellation() {
    assertThat(ConsultationStatus.CREATED.allowsCancellation()).isTrue();
    assertThat(ConsultationStatus.IN_PROGRESS.allowsCancellation()).isTrue();
    assertThat(ConsultationStatus.COMPLETED.allowsCancellation()).isFalse();
    assertThat(ConsultationStatus.CANCELLED.allowsCancellation()).isFalse();
  }
}

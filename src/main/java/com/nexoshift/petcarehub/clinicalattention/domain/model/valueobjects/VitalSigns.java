package com.nexoshift.petcarehub.clinicalattention.domain.model.valueobjects;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.math.BigDecimal;

/**
 * Basic measurements of a physiological assessment. Weight must be positive, temperature must be
 * between 30 and 45 degrees Celsius and the heart and respiratory rates must be positive.
 */
@Embeddable
public record VitalSigns(
    @Column(nullable = false, precision = 5, scale = 2) BigDecimal weightKg,
    @Column(nullable = false, precision = 4, scale = 1) BigDecimal temperatureCelsius,
    @Column(nullable = false) Integer heartRateBpm,
    @Column(nullable = false) Integer respiratoryRateBpm) {

  private static final BigDecimal MAX_WEIGHT_KG = new BigDecimal("500");
  private static final BigDecimal MIN_TEMPERATURE = new BigDecimal("30");
  private static final BigDecimal MAX_TEMPERATURE = new BigDecimal("45");
  private static final int MAX_HEART_RATE_BPM = 600;
  private static final int MAX_RESPIRATORY_RATE_BPM = 300;

  public VitalSigns {
    if (weightKg == null || weightKg.signum() <= 0 || weightKg.compareTo(MAX_WEIGHT_KG) > 0) {
      throw new IllegalArgumentException("Weight (kg) must be greater than 0 and at most 500");
    }
    if (temperatureCelsius == null || temperatureCelsius.compareTo(MIN_TEMPERATURE) < 0
        || temperatureCelsius.compareTo(MAX_TEMPERATURE) > 0) {
      throw new IllegalArgumentException("Temperature (Celsius) must be between 30 and 45");
    }
    if (heartRateBpm == null || heartRateBpm <= 0 || heartRateBpm > MAX_HEART_RATE_BPM) {
      throw new IllegalArgumentException("Heart rate (bpm) must be greater than 0 and at most 600");
    }
    if (respiratoryRateBpm == null || respiratoryRateBpm <= 0
        || respiratoryRateBpm > MAX_RESPIRATORY_RATE_BPM) {
      throw new IllegalArgumentException(
          "Respiratory rate (bpm) must be greater than 0 and at most 300");
    }
  }
}

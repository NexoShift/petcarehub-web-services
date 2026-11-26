package com.nexoshift.petcarehub.medicalhistory.domain.model.entities;

import com.nexoshift.petcarehub.medicalhistory.domain.model.valueobjects.MedicalHistoryFieldLimits;
import com.nexoshift.petcarehub.shared.domain.model.entities.AuditableModel;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;

import java.time.LocalDate;

/**
 * Vaccine applied to the pet (RegistroVacunacion), certified by a veterinarian of a clinic.
 * {@code nextDoseOn} is the date of the booster, if any.
 */
@Entity
public class VaccinationRecord extends AuditableModel {

  @Column(nullable = false, length = MedicalHistoryFieldLimits.VACCINE_NAME)
  private String vaccineName;

  private Integer doseNumber;

  @Column(nullable = false)
  private LocalDate appliedOn;

  private LocalDate nextDoseOn;

  @Column(length = MedicalHistoryFieldLimits.BATCH_NUMBER)
  private String batchNumber;

  @Column(length = MedicalHistoryFieldLimits.NOTES)
  private String notes;

  @Column(nullable = false)
  private Long veterinarianId;

  @Column(nullable = false)
  private Long clinicId;

  protected VaccinationRecord() {
  }

  public VaccinationRecord(String vaccineName, Integer doseNumber, LocalDate appliedOn,
      LocalDate nextDoseOn, String batchNumber, String notes, Long veterinarianId,
      Long clinicId) {
    if (vaccineName == null || vaccineName.isBlank()) {
      throw new IllegalArgumentException("Vaccine name is required");
    }
    if (appliedOn == null) {
      throw new IllegalArgumentException("Application date is required");
    }
    if (appliedOn.isAfter(LocalDate.now())) {
      throw new IllegalArgumentException("Application date cannot be in the future");
    }
    if (nextDoseOn != null && !nextDoseOn.isAfter(appliedOn)) {
      throw new IllegalArgumentException("Next dose date must be after the application date");
    }
    if (doseNumber != null
        && (doseNumber < 1 || doseNumber > MedicalHistoryFieldLimits.MAX_DOSE_NUMBER)) {
      throw new IllegalArgumentException("Dose number must be between 1 and "
          + MedicalHistoryFieldLimits.MAX_DOSE_NUMBER);
    }
    if (veterinarianId == null || clinicId == null) {
      throw new IllegalArgumentException("Veterinarian and clinic are required");
    }
    this.vaccineName = vaccineName;
    this.doseNumber = doseNumber;
    this.appliedOn = appliedOn;
    this.nextDoseOn = nextDoseOn;
    this.batchNumber = batchNumber;
    this.notes = notes;
    this.veterinarianId = veterinarianId;
    this.clinicId = clinicId;
  }

  public String getVaccineName() {
    return vaccineName;
  }

  public Integer getDoseNumber() {
    return doseNumber;
  }

  public LocalDate getAppliedOn() {
    return appliedOn;
  }

  public LocalDate getNextDoseOn() {
    return nextDoseOn;
  }

  public String getBatchNumber() {
    return batchNumber;
  }

  public String getNotes() {
    return notes;
  }

  public Long getVeterinarianId() {
    return veterinarianId;
  }

  public Long getClinicId() {
    return clinicId;
  }
}

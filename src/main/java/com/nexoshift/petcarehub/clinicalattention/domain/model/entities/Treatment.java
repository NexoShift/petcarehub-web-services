package com.nexoshift.petcarehub.clinicalattention.domain.model.entities;

import com.nexoshift.petcarehub.clinicalattention.domain.model.valueobjects.ClinicalFieldLimits;
import com.nexoshift.petcarehub.clinicalattention.domain.model.valueobjects.TreatmentStatus;
import com.nexoshift.petcarehub.clinicalattention.domain.model.valueobjects.VeterinarianId;
import com.nexoshift.petcarehub.shared.domain.model.entities.AuditableModel;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

import java.time.LocalDate;

/**
 * Therapeutic plan indicated by the veterinarian during the consultation (for example a diet,
 * cures or physiotherapy). Like every clinical record it is never overwritten (CON-01).
 * {@code endDate} is optional for treatments without a known end.
 */
@Entity
public class Treatment extends AuditableModel {

  @Column(nullable = false, length = ClinicalFieldLimits.TREATMENT_DESCRIPTION)
  private String description;

  @Column(nullable = false, length = ClinicalFieldLimits.TREATMENT_INSTRUCTION)
  private String instruction;

  @Column(nullable = false)
  private LocalDate startDate;

  private LocalDate endDate;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private TreatmentStatus treatmentStatus;

  @Embedded
  @AttributeOverride(name = "veterinarianId",
          column = @Column(name = "prescribed_by_veterinarian_id", nullable = false))
  private VeterinarianId prescribedBy;

  protected Treatment() {
  }

  public Treatment(String description, String instruction, LocalDate startDate,
                   LocalDate endDate, VeterinarianId prescribedBy) {
    if (description == null || description.isBlank()) {
      throw new IllegalArgumentException("Treatment description is required");
    }
    if (instruction == null || instruction.isBlank()) {
      throw new IllegalArgumentException("Treatment instruction is required");
    }
    if (startDate == null) {
      throw new IllegalArgumentException("Treatment start date is required");
    }
    if (endDate != null && endDate.isBefore(startDate)) {
      throw new IllegalArgumentException("Treatment end date cannot be before the start date");
    }
    if (prescribedBy == null) {
      throw new IllegalArgumentException("Veterinarian is required");
    }
    this.description = description;
    this.instruction = instruction;
    this.startDate = startDate;
    this.endDate = endDate;
    this.treatmentStatus = TreatmentStatus.ACTIVE;
    this.prescribedBy = prescribedBy;
  }

  public String getDescription() {
    return description;
  }

  public String getInstruction() {
    return instruction;
  }

  public LocalDate getStartDate() {
    return startDate;
  }

  public LocalDate getEndDate() {
    return endDate;
  }

  public TreatmentStatus getTreatmentStatus() {
    return treatmentStatus;
  }

  public VeterinarianId getPrescribedBy() {
    return prescribedBy;
  }
}

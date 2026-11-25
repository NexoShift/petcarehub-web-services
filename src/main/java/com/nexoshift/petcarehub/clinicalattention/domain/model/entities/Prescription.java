package com.nexoshift.petcarehub.clinicalattention.domain.model.entities;

import com.nexoshift.petcarehub.clinicalattention.domain.model.valueobjects.ClinicalFieldLimits;
import com.nexoshift.petcarehub.clinicalattention.domain.model.valueobjects.VeterinarianId;
import com.nexoshift.petcarehub.shared.domain.model.entities.AuditableModel;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Medical prescription (RecetaMedica) issued in a consultation. It is issued once with all its
 * medications and is never changed afterwards (CON-01): a new indication is a new prescription.
 * The issue date is set by the server, so it can never be in the future.
 */
@Entity
public class Prescription extends AuditableModel {

  @Column(nullable = false)
  private Instant issuedAt;

  @Column(length = ClinicalFieldLimits.PRESCRIPTION_INSTRUCTION)
  private String generalInstruction;

  @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
  @JoinColumn(name = "prescription_id", nullable = false, updatable = false)
  @OrderBy("id ASC")
  @Fetch(FetchMode.SUBSELECT)
  private List<Medication> medications = new ArrayList<>();

  @Embedded
  @AttributeOverride(name = "veterinarianId",
          column = @Column(name = "prescribed_by_veterinarian_id", nullable = false))
  private VeterinarianId prescribedBy;

  protected Prescription() {
  }

  public Prescription(String generalInstruction, List<Medication> medications,
                      VeterinarianId prescribedBy) {
    if (medications == null || medications.isEmpty()) {
      throw new IllegalArgumentException("A prescription must contain at least one medication");
    }
    if (medications.size() > ClinicalFieldLimits.MAX_MEDICATIONS) {
      throw new IllegalArgumentException("A prescription cannot have more than "
              + ClinicalFieldLimits.MAX_MEDICATIONS + " medications");
    }
    if (prescribedBy == null) {
      throw new IllegalArgumentException("Veterinarian is required");
    }
    this.issuedAt = Instant.now();
    this.generalInstruction = generalInstruction;
    this.medications.addAll(medications);
    this.prescribedBy = prescribedBy;
  }

  public Instant getIssuedAt() {
    return issuedAt;
  }

  public String getGeneralInstruction() {
    return generalInstruction;
  }

  public List<Medication> getMedications() {
    return List.copyOf(medications);
  }

  public VeterinarianId getPrescribedBy() {
    return prescribedBy;
  }
}

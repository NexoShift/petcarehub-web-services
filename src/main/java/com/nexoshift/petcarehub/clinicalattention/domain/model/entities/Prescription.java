package com.nexoshift.petcarehub.clinicalattention.domain.model.entities;

import com.nexoshift.petcarehub.clinicalattention.domain.exceptions.InvalidConsultationStateException;
import com.nexoshift.petcarehub.clinicalattention.domain.model.valueobjects.ClinicalFieldLimits;
import com.nexoshift.petcarehub.clinicalattention.interfaces.rest.resources.RegisterMedicationResource;
import com.nexoshift.petcarehub.shared.domain.model.entities.AuditableModel;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Prescription extends AuditableModel {

    @Column(nullable = false)
    private LocalDateTime dateAndTimeOfIssuance;

    @Column(nullable = false, length = ClinicalFieldLimits.PRESCRIPTION_INSTRUCTION)
    private String generalInstruction;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "prescription_id", nullable = false)
    private List<Medication> medications = new ArrayList<>();

    protected Prescription() {
    }

    public Prescription(LocalDateTime dateAndTimeOfIssuance, String generalInstruction, List<Medication> medications) {
        if (dateAndTimeOfIssuance == null) {
            throw new IllegalArgumentException("Date and time of issuance are required");
        }
        if (generalInstruction == null || generalInstruction.isBlank()) {
            throw new IllegalArgumentException("General instruction is required");
        }
        if (medications == null || medications.isEmpty()) {
            throw new IllegalArgumentException("A prescription must contain at least one medication");
        }
        this.dateAndTimeOfIssuance = dateAndTimeOfIssuance;
        this.generalInstruction = generalInstruction;
        this.medications.addAll(medications);
    }

    public void addMedication(Medication medication) {
        if (medication == null) {
            throw new IllegalArgumentException("Medication cannot be null");
        }
        this.medications.add(medication);
    }

    public void removeMedication(Medication medication) {
        if (this.medications.size() <= 1) {
            throw new InvalidConsultationStateException("A prescription must contain at least one medication");
        }
        this.medications.remove(medication);
    }

    public LocalDateTime getDateAndTimeOfIssuance() {
        return dateAndTimeOfIssuance;
    }

    public String getGeneralInstruction() {
        return generalInstruction;
    }

    public List<Medication> getMedications() {
        return List.copyOf(medications);
    }
}

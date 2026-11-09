package com.nexoshift.petcarehub.clinicalattention.domain.model.entities;

import com.nexoshift.petcarehub.clinicalattention.domain.model.valueobjects.ClinicalFieldLimits;
import com.nexoshift.petcarehub.clinicalattention.domain.model.valueobjects.TreatmentStatus;
import com.nexoshift.petcarehub.shared.domain.model.entities.AuditableModel;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

import java.time.LocalDateTime;

@Entity
public class Treatment extends AuditableModel {

    @Column(nullable = false, length = ClinicalFieldLimits.TREATMENT_DESCRIPTION)
    private String description;

    @Column(nullable = false, length = ClinicalFieldLimits.TREATMENT_INSTRUCTION)
    private String instruction;

    @Column(nullable = false)
    private LocalDateTime startDateTime;

    @Column(nullable = false)
    private LocalDateTime endDateTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TreatmentStatus treatmentStatus;

    protected Treatment() {
    }

    public Treatment(String description, String instruction, LocalDateTime startDateTime, LocalDateTime endDateTime) {
        if (description == null || description.isBlank()) {
            throw new IllegalArgumentException("Treatment description is required");
        }
        if (instruction == null || instruction.isBlank()) {
            throw new IllegalArgumentException("Treatment instruction is required");
        }
        if (startDateTime == null) {
            throw new IllegalArgumentException("Start date and time are required");
        }
        if (endDateTime == null) {
            throw new IllegalArgumentException("End date and time are required");
        }
        this.description = description;
        this.instruction = instruction;
        this.startDateTime = startDateTime;
        this.endDateTime = endDateTime;
        this.treatmentStatus = TreatmentStatus.ACTIVE;
    }

    public String getDescription() {return description;}
    public String getInstruction() {return instruction;}
    public LocalDateTime getStartDateTime() {return startDateTime;}
    public LocalDateTime getEndDateTime() {return endDateTime;}
    public TreatmentStatus getTreatmentStatus() {return treatmentStatus;}
}

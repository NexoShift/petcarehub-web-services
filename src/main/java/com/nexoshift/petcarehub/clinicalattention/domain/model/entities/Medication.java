package com.nexoshift.petcarehub.clinicalattention.domain.model.entities;

import com.nexoshift.petcarehub.clinicalattention.domain.model.valueobjects.ClinicalFieldLimits;
import com.nexoshift.petcarehub.shared.domain.model.entities.AuditableModel;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;

@Entity
public class Medication extends AuditableModel {
    @Column(nullable = false, length = ClinicalFieldLimits.MEDICATION_NAME)
    private String name;

    @Column(nullable = false, length = ClinicalFieldLimits.MEDICATION_DOSAGE)
    private String dosage;

    @Column(nullable = false, length = ClinicalFieldLimits.MEDICATION_FREQUENCY)
    private String frequency;

    @Column(nullable = false, length = ClinicalFieldLimits.MEDICATION_DURATION)
    private String duration;

    @Column(length = ClinicalFieldLimits.MEDICATION_INDICATIONS)
    private String indication;

    protected Medication() {
    }

    public Medication(String name, String dosage, String frequency, String duration, String indication) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Medication name is required");
        }
        if (dosage == null || dosage.isBlank()) {
            throw new IllegalArgumentException("Medication dosage is required");
        }
        if (frequency == null || frequency.isBlank()) {
            throw new IllegalArgumentException("Medication frequency is required");
        }
        if (duration == null || duration.isBlank()) {
            throw new IllegalArgumentException("Medication duration is required");
        }

        this.name = name;
        this.dosage = dosage;
        this.frequency = frequency;
        this.duration = duration;
        this.indication = indication;
    }

    public String getName() {return name;}

    public String getDosage() {return dosage;}

    public String getFrequency() {return frequency;}

    public String getDuration() {return duration;}

    public String getIndication() {return indication;}
}

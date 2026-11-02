package com.nexoshift.petcarehub.profiles.domain.model.aggregates;

import com.nexoshift.petcarehub.profiles.domain.model.commands.CreateClinicPatientCommand;
import com.nexoshift.petcarehub.profiles.domain.model.valueobjects.ClinicId;
import com.nexoshift.petcarehub.profiles.domain.model.valueobjects.PetId;
import com.nexoshift.petcarehub.shared.domain.model.aggregates.AuditableAbstractAggregateRoot;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import lombok.Getter;

import java.time.Instant;

/**
 * Aggregate root representing a pet registered as a patient of a clinic.
 */
@Getter
@Entity
public class ClinicPatient
        extends AuditableAbstractAggregateRoot<ClinicPatient> {

    @Embedded
    @AttributeOverride(name = "clinicId", column = @Column(name = "clinic_id", nullable = false))
    private ClinicId clinicId;

    @Embedded
    @AttributeOverride(name = "petId", column = @Column(name = "pet_id", nullable = false))
    private PetId petId;

    @Column(nullable = false, updatable = false)
    private Instant registeredAt;

    protected ClinicPatient() {
        super();
    }

    /**
     * Creates a clinic patient from a command.
     *
     * @param command the create clinic patient command
     */
    public ClinicPatient(CreateClinicPatientCommand command) {
        this();

        this.clinicId = new ClinicId(command.clinicId());
        this.petId = new PetId(command.petId());
        this.registeredAt = Instant.now();
    }
}

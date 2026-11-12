package com.nexoshift.petcarehub.profiles.domain.model.entities;

import com.nexoshift.petcarehub.profiles.domain.model.aggregates.Clinic;
import com.nexoshift.petcarehub.profiles.domain.model.valueobjects.ConsultingRoomId;
import com.nexoshift.petcarehub.profiles.domain.model.valueobjects.VeterinarianId;
import com.nexoshift.petcarehub.shared.domain.model.entities.AuditableModel;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;

import java.time.Instant;

@Getter
@Entity
public class ConsultingRoomAssignment extends AuditableModel {

    @Getter(AccessLevel.NONE)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "clinic_id", nullable = false)
    private Clinic clinic;

    @Embedded
    @AttributeOverride(name = "consultingRoomId", column = @Column(name = "consulting_room_id", nullable = false))
    private ConsultingRoomId consultingRoomId;

    @Embedded
    @AttributeOverride(name = "veterinarianId", column = @Column(name = "veterinarian_id", nullable = false))
    private VeterinarianId veterinarianId;

    @Column(nullable = false)
    private Instant assignedAt;

    private Instant unassignedAt;

    /**
     * Default constructor for JPA.
     */
    protected ConsultingRoomAssignment() {
        super();
    }

    /**
     * Creates a new consulting room assignment.
     */
    public ConsultingRoomAssignment(
            Clinic clinic,
            ConsultingRoomId consultingRoomId,
            VeterinarianId veterinarianId) {
        this();

        if (clinic == null) {
            throw new IllegalArgumentException("Clinic is required");
        }

        if (consultingRoomId == null) {
            throw new IllegalArgumentException(
                    "Consulting room ID is required");
        }

        if (veterinarianId == null) {
            throw new IllegalArgumentException(
                    "Veterinarian ID is required");
        }

        this.clinic = clinic;
        this.consultingRoomId = consultingRoomId;
        this.veterinarianId = veterinarianId;
        this.assignedAt = Instant.now();
    }

    /**
     * Indicates whether the assignment is currently active.
     */
    public boolean isActive() {
        return unassignedAt == null;
    }

    /**
     * Ends the current assignment while preserving its history.
     */
    public void unassign() {
        if (!isActive()) {
            throw new IllegalStateException(
                    "Consulting room assignment is already inactive");
        }

        this.unassignedAt = Instant.now();
    }
}

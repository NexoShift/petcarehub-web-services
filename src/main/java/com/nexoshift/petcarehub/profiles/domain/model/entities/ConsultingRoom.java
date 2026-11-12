package com.nexoshift.petcarehub.profiles.domain.model.entities;

import com.nexoshift.petcarehub.profiles.domain.model.aggregates.Clinic;
import com.nexoshift.petcarehub.profiles.domain.model.valueobjects.ClinicId;
import com.nexoshift.petcarehub.shared.domain.model.entities.AuditableModel;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;

@Getter
@Entity
public class ConsultingRoom extends AuditableModel {

    @Getter(AccessLevel.NONE)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "clinic_id", nullable = false)
    private Clinic clinic;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 500)
    private String description;

    @Column(nullable = false)
    private boolean active;

    /**
     * Default constructor for JPA.
     */
    protected ConsultingRoom(){
        super();
    }

    /**
     * Creates a consulting room belonging to a clinic.
     */
    public ConsultingRoom(Clinic clinic, String name, String description){

        this();
        if(clinic == null){
            throw new IllegalArgumentException("Clinic is required");
        }

        validateName(name);
        this.clinic = clinic;
        this.name = name.trim();
        this.description = description;
        this.active = true;
    }

    /**
     * Updates the consulting room information.
     */
    public void updateDetails(String name, String description){
        validateName(name);
        this.name = name.trim();
        this.description = description;
    }

    /**
     * Activates the consulting room.
     */
    public void activate(){
        this.active = true;
    }

    /**
     * Deactivates the consulting room.
     */
    public void deactivate(){
        this.active = false;
    }

    /**
     * Returns the identifier of the clinic.
     */
    public ClinicId getClinicId(){
        return new ClinicId(clinic.getId());
    }

    public static void validateName(String name){
        if(name == null ||  name.isBlank()){
            throw new IllegalArgumentException("Consulting room name is required");
        }

        if(name.trim().length() > 100){
            throw new IllegalArgumentException("Consulting room name cannot exceed 100 characters");
        }
    }
}

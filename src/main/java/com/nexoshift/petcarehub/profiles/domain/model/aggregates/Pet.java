package com.nexoshift.petcarehub.profiles.domain.model.aggregates;

import com.nexoshift.petcarehub.profiles.domain.model.commands.CreatePetCommand;
import com.nexoshift.petcarehub.profiles.domain.model.valueobjects.OwnerId;
import com.nexoshift.petcarehub.profiles.domain.model.valueobjects.PetSex;
import com.nexoshift.petcarehub.profiles.domain.model.valueobjects.Species;
import com.nexoshift.petcarehub.shared.domain.model.aggregates.AuditableAbstractAggregateRoot;
import jakarta.persistence.*;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Entity
public class Pet extends AuditableAbstractAggregateRoot<Pet> {

    @Embedded
    @AttributeOverride(name = "ownerId", column = @Column(name = "owner_id", nullable = false))
    private OwnerId ownerId;

    @Column(nullable = false, length = 100)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Species species;

    @Column(length = 100)
    private String breed;

    private LocalDate birthDate;

    @Enumerated(EnumType.STRING)
    private PetSex sex;

    @Column(length = 500)
    private String profileImageKey;

    protected Pet(){
        super();
    }

    /**
     * Creates a pet profile from a command.
     */
    public Pet(CreatePetCommand command) {
        this();

        validateName(command.name());

        if (command.species() == null) {
            throw new IllegalArgumentException("Species is required");
        }

        if (command.birthDate() != null
                && command.birthDate().isAfter(LocalDate.now())) {
            throw new IllegalArgumentException(
                    "Birth date cannot be in the future");
        }

        this.ownerId = new OwnerId(command.ownerId());
        this.name = command.name().trim();
        this.species = command.species();
        this.breed = command.breed();
        this.birthDate = command.birthDate();
        this.sex = command.sex();
        this.profileImageKey = command.profileImageKey();
    }

    public void updateProfile(String name, Species species, String breed, LocalDate birthDate, PetSex sex){
        validateName(name);

        if(species == null){
            throw new IllegalArgumentException("Pet species is required");
        }
        if(birthDate != null && birthDate.isAfter(LocalDate.now())){
            throw new IllegalArgumentException("Birth date cannot be in the future");
        }

        this.name = name.trim();
        this.species = species;
        this.breed = breed;
        this.birthDate = birthDate;
        this.sex = sex;
    }

    public void updateProfileImage(String profileImageKey){

        this.profileImageKey = profileImageKey;
    }

    private static void validateName(String name){
        if(name == null || name.isBlank()){
            throw new IllegalArgumentException("Pet name is required");
        }
    }
}

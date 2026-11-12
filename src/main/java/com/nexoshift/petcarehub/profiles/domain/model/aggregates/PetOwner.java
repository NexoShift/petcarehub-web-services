package com.nexoshift.petcarehub.profiles.domain.model.aggregates;

import com.nexoshift.petcarehub.profiles.domain.model.commands.CreatePetOwnerCommand;
import com.nexoshift.petcarehub.profiles.domain.model.valueobjects.PersonSex;
import com.nexoshift.petcarehub.profiles.domain.model.valueobjects.UserId;
import com.nexoshift.petcarehub.shared.domain.model.aggregates.AuditableAbstractAggregateRoot;
import jakarta.persistence.*;
import lombok.Getter;

@Getter
@Entity
public class PetOwner extends AuditableAbstractAggregateRoot<PetOwner> {

    @Embedded
    @AttributeOverride(name = "userId", column = @Column(name = "user_id", nullable = false, unique = true))
    private UserId userId;

    @Column(length = 100)
    private String firstName;

    @Column(length = 100)
    private String lastName;

    @Column(length = 255)
    private String email;

    @Column(length = 12)
    private String phoneNumber;

    @Enumerated(EnumType.STRING)
    private PersonSex sex;

    @Column(length = 500)
    private String profileImageKey;

    public PetOwner(){
        super();
    }

    public PetOwner(UserId userId, String email){
        this();
        if(userId == null) throw new IllegalArgumentException("User ID is required");
        this.userId = userId;
        this.email = email;
    }

    /**
     * Creates a pet owner profile from a command.
     */
    public PetOwner(CreatePetOwnerCommand command) {
        this();

        this.userId = new UserId(command.userId());
        this.firstName = command.firstName();
        this.lastName = command.lastName();
        this.email = command.email();
        this.phoneNumber = command.phoneNumber();
        this.sex = command.sex();
        this.profileImageKey = command.profileImageKey();
    }


    public void updateProfile(String firstName, String lastName, String phoneNumber, PersonSex sex){
        this.firstName = firstName;
        this.lastName = lastName;
        this.phoneNumber = phoneNumber;
        this.sex = sex;
    }

    public void updateEmail(String email){
        this.email = email.trim();
    }

    public void updateProfileImage(String profileImageKey){
        this.profileImageKey = profileImageKey;
    }
}

package com.nexoshift.petcarehub.profiles.domain.model.aggregates;

import com.nexoshift.petcarehub.profiles.domain.model.commands.CreateVeterinarianCommand;
import com.nexoshift.petcarehub.profiles.domain.model.valueobjects.ClinicId;
import com.nexoshift.petcarehub.profiles.domain.model.valueobjects.PersonSex;
import com.nexoshift.petcarehub.profiles.domain.model.valueobjects.UserId;
import com.nexoshift.petcarehub.shared.domain.model.aggregates.AuditableAbstractAggregateRoot;
import jakarta.persistence.*;
import lombok.Getter;

@Getter
@Entity
public class Veterinarian extends AuditableAbstractAggregateRoot<Veterinarian> {

    @Embedded
    @AttributeOverride(name = "userId", column = @Column(name = "user_id", nullable = false,unique = true))
    private UserId userId;

    @Embedded
    @AttributeOverride(name = "clinicId", column = @Column(name = "clinic_id", nullable = false))
    private ClinicId clinicId;

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

    @Column(length = 50)
    private String licenseNumber;

    @Column(length = 100)
    private String specialty;

    @Column(length = 500)
    private String profileImageKey;

    protected Veterinarian(){
        super();
    }

    public Veterinarian(UserId userId, ClinicId clinicId, String email){
        this();
        if(userId == null){
            throw new IllegalArgumentException("User ID is required");
        }
        if(clinicId == null){
            throw new IllegalArgumentException("Clinic ID is required");
        }
        this.userId = userId;
        this.clinicId = clinicId;
        this.email = email;
    }

    /**
     * Creates a veterinarian profile from a command.
     */
    public Veterinarian(CreateVeterinarianCommand command) {
        this();

        this.userId = new UserId(command.userId());
        this.clinicId = new ClinicId(command.clinicId());
        this.firstName = command.firstName();
        this.lastName = command.lastName();
        this.email = command.email();
        this.phoneNumber = command.phoneNumber();
        this.sex = command.sex();
        this.licenseNumber = command.licenseNumber();
        this.specialty = command.specialty();
        this.profileImageKey = command.profileImageKey();
    }

    public void updateProfile(String firstName, String lastName, String phoneNumber,
                              PersonSex sex, String licenseNumber, String specialty){
        this.firstName = firstName;
        this.lastName = lastName;
        this.phoneNumber = phoneNumber;
        this.sex = sex;
        this.licenseNumber = licenseNumber;
        this.specialty = specialty;
    }

    public void updateEmail(String email){
        this.email = email;
    }

    public void updateProfileImage(String profileImageKey){
        this.profileImageKey = profileImageKey;
    }
}

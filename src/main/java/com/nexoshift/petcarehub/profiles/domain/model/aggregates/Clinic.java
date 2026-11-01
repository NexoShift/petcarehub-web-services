package com.nexoshift.petcarehub.profiles.domain.model.aggregates;

import com.nexoshift.petcarehub.profiles.domain.exceptions.ConsultingRoomAssignmentConflictException;
import com.nexoshift.petcarehub.profiles.domain.exceptions.ConsultingRoomAssignmentNotFoundException;
import com.nexoshift.petcarehub.profiles.domain.exceptions.ConsultingRoomNotFoundException;
import com.nexoshift.petcarehub.profiles.domain.exceptions.ProfileOperationNotAllowedException;
import com.nexoshift.petcarehub.profiles.domain.model.commands.CreateClinicCommand;
import com.nexoshift.petcarehub.profiles.domain.model.entities.ConsultingRoom;
import com.nexoshift.petcarehub.profiles.domain.model.entities.ConsultingRoomAssignment;
import com.nexoshift.petcarehub.profiles.domain.model.valueobjects.AccountId;
import com.nexoshift.petcarehub.profiles.domain.model.valueobjects.ConsultingRoomId;
import com.nexoshift.petcarehub.profiles.domain.model.valueobjects.VeterinarianId;
import com.nexoshift.petcarehub.shared.domain.model.aggregates.AuditableAbstractAggregateRoot;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Getter
@Entity
public class Clinic extends AuditableAbstractAggregateRoot<Clinic> {

    @Embedded
    @AttributeOverride(name = "accountId", column = @Column(name = "account_id", nullable = false, unique = true))
    private AccountId accountId;

    @Column(length = 100)
    private String name;

    @Column(length = 11)
    private String ruc;

    @Column(length = 150)
    private String address;

    @Column(length = 12)
    private String phoneNumber;

    @Column(length = 500)
    private String description;

    @Column(length = 500)
    private String profileImageKey;

    @Getter(AccessLevel.NONE)
    @OneToMany(mappedBy = "clinic", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id ASC")
    private List<ConsultingRoom> consultingRooms = new ArrayList<>();

    @Getter(AccessLevel.NONE)
    @OneToMany(mappedBy = "clinic", cascade = CascadeType.ALL)
    @OrderBy("id ASC")
    private List<ConsultingRoomAssignment> assignments = new ArrayList<>();

    protected Clinic(){
        super();
    }

    /**
     * Creates an initial clinic profile associated with an IAM account.
     */
    public Clinic(AccountId accountId){
        this();
        if(accountId == null){
            throw new IllegalArgumentException("Account ID is requited");
        }
        this.accountId = accountId;
    }

    /**
     * Creates a clinic profile from a command.
     */
    public Clinic(CreateClinicCommand command) {
        this();

        validateRuc(command.ruc());

        this.accountId = new AccountId(command.accountId());
        this.name = command.name();
        this.ruc = command.ruc();
        this.address = command.address();
        this.phoneNumber = command.phoneNumber();
        this.description = command.description();
        this.profileImageKey = command.profileImageKey();
    }

    /**
     * Updates the clinic's institutional information.
     */
    public void updateProfile(
            String name,
            String ruc,
            String address,
            String phoneNumber,
            String description) {

        validateRuc(ruc);

        this.name = name;
        this.ruc = ruc;
        this.address = address;
        this.phoneNumber = phoneNumber;
        this.description = description;
    }

    /**
     * Updates the clinic's profile image.
     */

    public void updateProfileImage(String profileImageKey){
        this.profileImageKey = profileImageKey;
    }

    /**
     * Adds a consulting room to the clinic.
     */
    public ConsultingRoom addConsultingRoom(String name, String description){

        validateConsultingRoomName(name);

        boolean duplicatedName = consultingRooms.stream()
                .anyMatch(room -> room.getName().equalsIgnoreCase(name.trim()));

        if(duplicatedName){
            throw new IllegalArgumentException("A consulting room with this name already exists");
        }

        var consultingRoom = new ConsultingRoom(this, name, description);

        this.consultingRooms.add(consultingRoom);

        return consultingRoom;
    }

    public void updateConsultingRoom(Long consultingRoomId, String name, String description){

        validateConsultingRoomName(name);
        var room = findConsultingRoom(consultingRoomId);

        boolean duplicatedName = consultingRooms.stream()
                .anyMatch(item -> !item.getId().equals(consultingRoomId)
                && item.getName().equalsIgnoreCase(name.trim()));

        if(duplicatedName){
            throw new IllegalArgumentException("A consulting room with this name already exists");
        }

        room.updateDetails(name,description);
    }

    public void activateConsultingRoom(Long consultingRoomId){

        findConsultingRoom(consultingRoomId).activate();
    }

    public void deactivateConsultingRoom(Long consultingRoomId){

        var room = findConsultingRoom(consultingRoomId);
        boolean hasActiveAssignments = assignments.stream()
                .anyMatch(assignment -> assignment.getConsultingRoomId()
                        .consultingRoomId().equals(consultingRoomId) && assignment.isActive());

        if(hasActiveAssignments){
            throw new ConsultingRoomAssignmentConflictException("Cannot deactivate a room with active assignments");
        }
        room.deactivate();
    }

    public void removeConsultingRoom(Long consultingRoomId){

        var room = findConsultingRoom(consultingRoomId);

        boolean hasAssignmentHistory = assignments.stream()
                .anyMatch(assignment -> assignment.getConsultingRoomId()
                        .consultingRoomId().equals(consultingRoomId));

        if(hasAssignmentHistory){
            throw new ConsultingRoomAssignmentConflictException("Cannot delete a room with assignment history");
        }
        consultingRooms.remove(room);
    }

    public void assignVeterinarian(ConsultingRoomId consultingRoomId, VeterinarianId veterinarianId){

        if(consultingRoomId == null) throw new IllegalArgumentException("Consulting room ID is required");
        if(veterinarianId == null) throw new IllegalArgumentException("Veterinarian Id is required");

        var room = findConsultingRoom(consultingRoomId.consultingRoomId());

        if(!room.isActive()){
            throw new ProfileOperationNotAllowedException("Cannot assign a veterinarian to a inactive room");
        }

        var activeAssignment = assignments.stream()
                .filter(ConsultingRoomAssignment::isActive)
                .filter(assignment ->
                        assignment.getConsultingRoomId().equals(consultingRoomId))
                .findFirst();

        if (activeAssignment.isPresent()) {
            var currentAssignment = activeAssignment.get();

            if (currentAssignment.getVeterinarianId().equals(veterinarianId)) {
                return;
            }

            currentAssignment.unassign();
        }

        assignments.add( new ConsultingRoomAssignment(this, consultingRoomId, veterinarianId));

    }

    public void unassignVeterinarian(Long assignmentId){

        var assignment = assignments.stream()
                .filter(item -> item.getId().equals(assignmentId))
                .findFirst()
                .orElseThrow(()-> new ConsultingRoomAssignmentNotFoundException(assignmentId));

        if(!assignment.isActive()){
            throw new ConsultingRoomAssignmentConflictException("Consulting room assignment is already inactive");
        }

        assignment.unassign();
    }

    private ConsultingRoom findConsultingRoom(Long consultingRoomId){
        return consultingRooms.stream()
                .filter(room -> room.getId().equals(consultingRoomId))
                .findFirst()
                .orElseThrow(() ->
                        new ConsultingRoomNotFoundException(consultingRoomId));
    }

    private static void validateConsultingRoomName(String name){
        if(name == null ||  name.isBlank()){
            throw new IllegalArgumentException("Consulting room name is required");
        }
    }

    /**
     * Validates the clinic's tax identification number.
     */
    private static void validateRuc(String ruc) {
        if (ruc != null && !ruc.matches("\\d{11}")) {
            throw new IllegalArgumentException(
                    "RUC must contain exactly 11 digits");
        }
    }

    /**
     * Returns an unmodifiable view of the consulting rooms.
     */
    public List<ConsultingRoom> getConsultingRooms() {
        return Collections.unmodifiableList(consultingRooms);
    }

    /**
     * Returns an unmodifiable view of the consulting room assignments.
     */
    public List<ConsultingRoomAssignment> getAssignments() {
        return Collections.unmodifiableList(assignments);
    }
}

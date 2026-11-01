package com.nexoshift.petcarehub.profiles.application.internal.commandservices;

import com.nexoshift.petcarehub.profiles.domain.exceptions.ClinicNotFoundException;
import com.nexoshift.petcarehub.profiles.domain.exceptions.ProfileAlreadyExistsException;
import com.nexoshift.petcarehub.profiles.domain.exceptions.ProfileOperationNotAllowedException;
import com.nexoshift.petcarehub.profiles.domain.exceptions.VeterinarianNotFoundException;
import com.nexoshift.petcarehub.profiles.domain.model.aggregates.Clinic;
import com.nexoshift.petcarehub.profiles.domain.model.commands.*;
import com.nexoshift.petcarehub.profiles.domain.model.valueobjects.AccountId;
import com.nexoshift.petcarehub.profiles.domain.model.valueobjects.ClinicId;
import com.nexoshift.petcarehub.profiles.domain.model.valueobjects.ConsultingRoomId;
import com.nexoshift.petcarehub.profiles.domain.model.valueobjects.VeterinarianId;
import com.nexoshift.petcarehub.profiles.domain.services.ClinicCommandService;
import com.nexoshift.petcarehub.profiles.infrastructure.persistence.jpa.repositories.ClinicRepository;
import com.nexoshift.petcarehub.profiles.infrastructure.persistence.jpa.repositories.VeterinarianRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Implementation of the clinic command service.
 * Handles clinic profiles, consulting rooms, and veterinarian assignments.
 *
 * @see ClinicCommandService
 * @see ClinicRepository
 */
@Service
public class ClinicCommandServiceImpl implements ClinicCommandService {

    private final ClinicRepository clinicRepository;
    private final VeterinarianRepository veterinarianRepository;

    /**
     * Constructs the clinic command service.
     *
     * @param clinicRepository the clinic repository
     * @param veterinarianRepository the veterinarian repository
     */
    public ClinicCommandServiceImpl(
            ClinicRepository clinicRepository,
            VeterinarianRepository veterinarianRepository) {
        this.clinicRepository = clinicRepository;
        this.veterinarianRepository = veterinarianRepository;
    }

    /**
     * Creates a clinic profile.
     *
     * @param command the create clinic command
     * @return the created clinic identifier
     */
    @Override
    public Long handle(CreateClinicCommand command) {

        var accountId = new AccountId(command.accountId());

        if (clinicRepository.existsByAccountId(accountId)) {
            throw new ProfileAlreadyExistsException(
                    "Clinic", command.accountId());
        }

        var clinic = new Clinic(command);

        return clinicRepository.save(clinic).getId();
    }

    /**
     * Updates the clinic's institutional information.
     *
     * @param command the update clinic command
     * @return the updated clinic
     */
    @Override
    public Optional<Clinic> handle(UpdateClinicCommand command) {

        var clinic = findClinic(command.clinicId());

        clinic.updateProfile(
                command.name(),
                command.ruc(),
                command.address(),
                command.phoneNumber(),
                command.description()
        );

        return Optional.of(clinicRepository.save(clinic));
    }

    /**
     * Updates the clinic's profile image.
     *
     * @param command the update clinic image command
     * @return the updated clinic
     */
    @Override
    public Optional<Clinic> handle(UpdateClinicImageCommand command) {

        var clinic = findClinic(command.clinicId());

        clinic.updateProfileImage(command.profileImageKey());

        return Optional.of(clinicRepository.save(clinic));
    }

    /**
     * Creates a consulting room within a clinic.
     *
     * @param command the create consulting room command
     */
    @Override
    @Transactional
    public Long handle(CreateConsultingRoomCommand command) {

        var clinic = clinicRepository.findById(command.clinicId())
                .orElseThrow(() -> new ClinicNotFoundException(command.clinicId()));

        var consultingRoom = clinic.addConsultingRoom(
                command.name(),
                command.description()
        );

        clinicRepository.flush();
        return consultingRoom.getId();
    }

    /**
     * Updates a consulting room.
     *
     * @param command the update consulting room command
     */
    @Override
    @Transactional
    public void handle(UpdateConsultingRoomCommand command) {

        var clinic = findClinic(command.clinicId());

        clinic.updateConsultingRoom(
                command.consultingRoomId(),
                command.name(),
                command.description()
        );

        clinicRepository.save(clinic);
    }

    /**
     * Activates a consulting room.
     *
     * @param command the activate consulting room command
     */
    @Override
    @Transactional
    public void handle(ActivateConsultingRoomCommand command) {

        var clinic = findClinic(command.clinicId());

        clinic.activateConsultingRoom(command.consultingRoomId());

        clinicRepository.save(clinic);
    }

    /**
     * Deactivates a consulting room without active assignments.
     *
     * @param command the deactivate consulting room command
     */
    @Override
    @Transactional
    public void handle(DeactivateConsultingRoomCommand command) {

        var clinic = findClinic(command.clinicId());

        clinic.deactivateConsultingRoom(command.consultingRoomId());

        clinicRepository.save(clinic);
    }

    /**
     * Deletes a consulting room without assignment history.
     *
     * @param command the delete consulting room command
     */
    @Override
    @Transactional
    public void handle(DeleteConsultingRoomCommand command) {

        var clinic = findClinic(command.clinicId());

        clinic.removeConsultingRoom(command.consultingRoomId());

        clinicRepository.save(clinic);
    }

    /**
     * Assigns a veterinarian to a consulting room.
     * Automatically replaces the previous active assignment.
     *
     * @param command the assign veterinarian command
     */
    @Override
    @Transactional
    public void handle(AssignVeterinarianCommand command) {

        var clinic = clinicRepository.findById(command.clinicId())
                .orElseThrow(() ->
                        new ClinicNotFoundException(command.clinicId())
                );

        var veterinarian = veterinarianRepository.findById(
                command.veterinarianId()
        ).orElseThrow(() ->
                new VeterinarianNotFoundException(command.veterinarianId()));

        if (!veterinarian.getClinicId().clinicId().equals(command.clinicId())) {
            throw new ProfileOperationNotAllowedException(
                    "Veterinarian does not belong to this clinic"
            );
        }

        clinic.assignVeterinarian(
                new ConsultingRoomId(command.consultingRoomId()),
                new VeterinarianId(command.veterinarianId())
        );

        clinicRepository.flush();
    }

    /**
     * Ends an active veterinarian assignment.
     *
     * @param command the unassign veterinarian command
     */
    @Override
    @Transactional
    public void handle(UnassignVeterinarianCommand command) {

        var clinic = findClinic(command.clinicId());

        clinic.unassignVeterinarian(command.assignmentId());

        clinicRepository.save(clinic);
    }

    /**
     * Retrieves a clinic or throws an exception if it does not exist.
     *
     * @param clinicId the clinic identifier
     * @return the clinic aggregate
     */
    private Clinic findClinic(Long clinicId) {
        return clinicRepository.findById(clinicId)
                .orElseThrow(() ->
                        new ClinicNotFoundException(clinicId));
    }
}

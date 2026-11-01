package com.nexoshift.petcarehub.profiles.application.internal.commandservices;

import com.nexoshift.petcarehub.profiles.domain.exceptions.ClinicNotFoundException;
import com.nexoshift.petcarehub.profiles.domain.exceptions.ProfileAlreadyExistsException;
import com.nexoshift.petcarehub.profiles.domain.exceptions.VeterinarianNotFoundException;
import com.nexoshift.petcarehub.profiles.domain.model.aggregates.Veterinarian;
import com.nexoshift.petcarehub.profiles.domain.model.commands.CreateVeterinarianCommand;
import com.nexoshift.petcarehub.profiles.domain.model.commands.UpdateVeterinarianCommand;
import com.nexoshift.petcarehub.profiles.domain.model.commands.UpdateVeterinarianImageCommand;
import com.nexoshift.petcarehub.profiles.domain.model.valueobjects.UserId;
import com.nexoshift.petcarehub.profiles.domain.services.VeterinarianCommandService;
import com.nexoshift.petcarehub.profiles.infrastructure.persistence.jpa.repositories.ClinicRepository;
import com.nexoshift.petcarehub.profiles.infrastructure.persistence.jpa.repositories.VeterinarianRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Implementation of the veterinarian command service.
 * Handles veterinarian profile creation and updates.
 *
 * @see VeterinarianCommandService
 * @see VeterinarianRepository
 */
@Service
public class VeterinarianCommandServiceImpl implements VeterinarianCommandService {

    private final VeterinarianRepository veterinarianRepository;
    private final ClinicRepository clinicRepository;

    /**
     * Constructs the veterinarian command service.
     *
     * @param veterinarianRepository the veterinarian repository
     * @param clinicRepository the clinic repository
     */
    public VeterinarianCommandServiceImpl(
            VeterinarianRepository veterinarianRepository,
            ClinicRepository clinicRepository) {
        this.veterinarianRepository = veterinarianRepository;
        this.clinicRepository = clinicRepository;
    }

    /**
     * Creates a veterinarian profile associated with an existing clinic.
     *
     * @param command the create veterinarian command
     * @return the created veterinarian identifier
     */
    @Override
    public Long handle(CreateVeterinarianCommand command) {

        if (!clinicRepository.existsById(command.clinicId())) {
            throw new ClinicNotFoundException(command.clinicId());
        }

        var userId = new UserId(command.userId());

        if (veterinarianRepository.existsByUserId(userId)) {
            throw new ProfileAlreadyExistsException(
                    "Veterinarian", command.userId());
        }

        var veterinarian = new Veterinarian(command);

        return veterinarianRepository.save(veterinarian).getId();
    }

    /**
     * Updates a veterinarian's personal and professional information.
     *
     * @param command the update veterinarian command
     * @return the updated veterinarian
     */
    @Override
    public Optional<Veterinarian> handle(UpdateVeterinarianCommand command) {

        var veterinarian = veterinarianRepository.findById(command.veterinarianId())
                .orElseThrow(() ->
                        new VeterinarianNotFoundException(command.veterinarianId()));

        veterinarian.updateProfile(
                command.firstName(),
                command.lastName(),
                command.phoneNumber(),
                command.sex(),
                command.licenseNumber(),
                command.specialty()
        );

        return Optional.of(veterinarianRepository.save(veterinarian));
    }

    /**
     * Updates a veterinarian's profile image.
     *
     * @param command the update veterinarian image command
     * @return the updated veterinarian
     */
    @Override
    public Optional<Veterinarian> handle(UpdateVeterinarianImageCommand command) {

        var veterinarian = veterinarianRepository.findById(command.veterinarianId())
                .orElseThrow(() ->
                        new VeterinarianNotFoundException(command.veterinarianId()));

        veterinarian.updateProfileImage(command.profileImageKey());

        return Optional.of(veterinarianRepository.save(veterinarian));
    }
}

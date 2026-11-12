package com.nexoshift.petcarehub.profiles.application.internal.commandservices;

import com.nexoshift.petcarehub.profiles.application.internal.outboundservices.acl.IamExternalService;
import com.nexoshift.petcarehub.profiles.domain.exceptions.ClinicNotFoundException;
import com.nexoshift.petcarehub.profiles.domain.exceptions.ProfileAlreadyExistsException;
import com.nexoshift.petcarehub.profiles.domain.exceptions.ProfileOperationNotAllowedException;
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
    private final IamExternalService iamExternalService;

    /**
     * Constructs the veterinarian command service.
     *
     * @param veterinarianRepository the veterinarian repository
     * @param clinicRepository the clinic repository
     */
    public VeterinarianCommandServiceImpl(
            VeterinarianRepository veterinarianRepository,
            ClinicRepository clinicRepository,
            IamExternalService iamExternalService) {
        this.veterinarianRepository = veterinarianRepository;
        this.clinicRepository = clinicRepository;
        this.iamExternalService = iamExternalService;
    }


    /**
     * Creates a veterinarian profile within an existing clinic.
     *
     * Only an administrator belonging to the clinic account
     * can register the professional profile of a veterinarian.
     *
     * @param command the create veterinarian command
     * @return the created veterinarian identifier
     */
    @Override
    public Long handle(CreateVeterinarianCommand command) {

        var clinic = clinicRepository.findById(command.clinicId())
                .orElseThrow(() ->
                        new ClinicNotFoundException(command.clinicId()));

        var clinicAccountId = clinic.getAccountId().accountId();

        // Identify the authenticated administrator.
        var authenticatedUserId = iamExternalService.fetchAuthenticatedUserId();

        // Verify that the requester is an active clinic administrator.
        if (!iamExternalService.existsActiveUser(authenticatedUserId)
                || !iamExternalService.userHasRole(
                authenticatedUserId, "ROLE_CLINIC_ADMIN")) {
            throw new ProfileOperationNotAllowedException(
                    "Only an active clinic administrator can create veterinarian profiles");
        }

        // Prevent administrators from managing profiles in other clinic accounts.
        var administratorAccountId =
                iamExternalService.fetchAccountIdByUserId(authenticatedUserId);

        if (!administratorAccountId.equals(clinicAccountId)) {
            throw new ProfileOperationNotAllowedException(
                    "Administrator does not belong to this clinic account");
        }

        // Verify that the target user exists and is active.
        if (!iamExternalService.existsActiveUser(command.userId())) {
            throw new ProfileOperationNotAllowedException(
                    "User does not exist or is inactive");
        }

        // Verify that the target user has the veterinarian role.
        if (!iamExternalService.userHasRole(
                command.userId(), "ROLE_VETERINARIAN")) {
            throw new ProfileOperationNotAllowedException(
                    "User does not have the veterinarian role");
        }

        // Verify that the target veterinarian belongs to this clinic account.
        var veterinarianAccountId =
                iamExternalService.fetchAccountIdByUserId(command.userId());

        if (!veterinarianAccountId.equals(clinicAccountId)
                || !"CLINIC".equals(
                iamExternalService.fetchAccountTypeByAccountId(
                        veterinarianAccountId))) {
            throw new ProfileOperationNotAllowedException(
                    "Veterinarian does not belong to this clinic account");
        }

        // Prevent duplicate professional profiles.
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

        var authenticatedUserId = iamExternalService.fetchAuthenticatedUserId();

        var veterinarianUserId = veterinarian.getUserId().userId();

        var clinic = clinicRepository.findById(veterinarian.getClinicId().clinicId())
                .orElseThrow(() -> new ClinicNotFoundException(veterinarian.getClinicId().clinicId()));

        var clinicAccountId = clinic.getAccountId().accountId();

        var isOwner = veterinarianUserId.equals(authenticatedUserId);

        var isClinicAdmin = iamExternalService.userHasRole(authenticatedUserId, "ROLE_CLINIC_ADMIN")
                && iamExternalService.fetchAccountIdByUserId(authenticatedUserId).equals(clinicAccountId);

        if (!isOwner && !isClinicAdmin) {
            throw new ProfileOperationNotAllowedException("You cannot update this veterinarian's profile");
        }

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
                .orElseThrow(() -> new VeterinarianNotFoundException(command.veterinarianId()));

        var authenticatedUserId = iamExternalService.fetchAuthenticatedUserId();

        var veterinarianUserId = veterinarian.getUserId().userId();

        var clinic = clinicRepository.findById(veterinarian.getClinicId().clinicId())
                .orElseThrow(() -> new ClinicNotFoundException(veterinarian.getClinicId().clinicId()));

        var clinicAccountId = clinic.getAccountId().accountId();

        var isOwner = veterinarianUserId.equals(authenticatedUserId);

        var isClinicAdmin = iamExternalService.userHasRole(authenticatedUserId, "ROLE_CLINIC_ADMIN")
                && iamExternalService.fetchAccountIdByUserId(authenticatedUserId).equals(clinicAccountId);

        if (!isOwner && !isClinicAdmin) {
            throw new ProfileOperationNotAllowedException("You cannot update this veterinarian's profile image");
        }

        veterinarian.updateProfileImage(command.profileImageKey());

        return Optional.of(veterinarianRepository.save(veterinarian));
    }
}

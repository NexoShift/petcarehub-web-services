package com.nexoshift.petcarehub.profiles.application.internal.commandservices;

import com.nexoshift.petcarehub.profiles.application.internal.outboundservices.acl.IamExternalService;
import com.nexoshift.petcarehub.profiles.domain.exceptions.ClinicNotFoundException;
import com.nexoshift.petcarehub.profiles.domain.exceptions.ClinicPatientAlreadyExistsException;
import com.nexoshift.petcarehub.profiles.domain.exceptions.PetNotFoundException;
import com.nexoshift.petcarehub.profiles.domain.exceptions.ProfileOperationNotAllowedException;
import com.nexoshift.petcarehub.profiles.domain.model.aggregates.ClinicPatient;
import com.nexoshift.petcarehub.profiles.domain.model.commands.CreateClinicPatientCommand;
import com.nexoshift.petcarehub.profiles.domain.model.valueobjects.ClinicId;
import com.nexoshift.petcarehub.profiles.domain.model.valueobjects.PetId;
import com.nexoshift.petcarehub.profiles.domain.services.ClinicPatientCommandService;
import com.nexoshift.petcarehub.profiles.infrastructure.persistence.jpa.repositories.ClinicPatientRepository;
import com.nexoshift.petcarehub.profiles.infrastructure.persistence.jpa.repositories.ClinicRepository;
import com.nexoshift.petcarehub.profiles.infrastructure.persistence.jpa.repositories.PetRepository;
import org.springframework.stereotype.Service;

/**
 * Implementation of the clinic patient command service.
 *
 * @see ClinicPatientCommandService
 * @see ClinicPatientRepository
 */
@Service
public class ClinicPatientCommandServiceImpl implements ClinicPatientCommandService {

    private final ClinicPatientRepository clinicPatientRepository;
    private final ClinicRepository clinicRepository;
    private final PetRepository petRepository;
    private final IamExternalService iamExternalService;

    /**
     * Constructs the clinic patient command service.
     *
     * @param clinicPatientRepository the clinic patient repository
     * @param clinicRepository the clinic repository
     * @param petRepository the pet repository
     */
    public ClinicPatientCommandServiceImpl(
            ClinicPatientRepository clinicPatientRepository,
            ClinicRepository clinicRepository,
            PetRepository petRepository,
            IamExternalService iamExternalService) {

        this.clinicPatientRepository = clinicPatientRepository;
        this.clinicRepository = clinicRepository;
        this.petRepository = petRepository;
        this.iamExternalService = iamExternalService;
    }

    /**
     * Registers a pet as a patient of a clinic.
     *
     * @param command the create clinic patient command
     * @return the created clinic patient identifier
     */
    @Override
    public Long handle(CreateClinicPatientCommand command) {

        var clinic = clinicRepository.findById(command.clinicId())
                .orElseThrow(() -> new ClinicNotFoundException(command.clinicId()));

        var authenticatedUserId = iamExternalService.fetchAuthenticatedUserId();

        var isClinicAdmin = iamExternalService.userHasRole(
                authenticatedUserId, "ROLE_CLINIC_ADMIN");

        var isVeterinarian = iamExternalService.userHasRole(
                authenticatedUserId, "ROLE_VETERINARIAN");

        var accountId = iamExternalService.fetchAccountIdByUserId(
                authenticatedUserId);

        if ((!isClinicAdmin && !isVeterinarian)
                || !clinic.getAccountId().accountId().equals(accountId)) {
            throw new ProfileOperationNotAllowedException("You are not authorized to register patients in this clinic");
        }

        if (!petRepository.existsById(command.petId())) {
            throw new PetNotFoundException(command.petId());
        }

        var clinicId = new ClinicId(command.clinicId());
        var petId = new PetId(command.petId());

        if (clinicPatientRepository.existsByClinicIdAndPetId(
                clinicId, petId)) {
            throw new ClinicPatientAlreadyExistsException(
                    command.clinicId(), command.petId());
        }

        var clinicPatient = new ClinicPatient(command);

        return clinicPatientRepository.save(clinicPatient).getId();
    }
}

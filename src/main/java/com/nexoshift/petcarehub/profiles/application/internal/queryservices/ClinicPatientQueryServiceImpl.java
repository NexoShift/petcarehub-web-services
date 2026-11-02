package com.nexoshift.petcarehub.profiles.application.internal.queryservices;

import com.nexoshift.petcarehub.profiles.application.internal.outboundservices.acl.IamExternalService;
import com.nexoshift.petcarehub.profiles.domain.exceptions.ClinicNotFoundException;
import com.nexoshift.petcarehub.profiles.domain.exceptions.PetNotFoundException;
import com.nexoshift.petcarehub.profiles.domain.exceptions.PetOwnerNotFoundException;
import com.nexoshift.petcarehub.profiles.domain.exceptions.ProfileOperationNotAllowedException;
import com.nexoshift.petcarehub.profiles.domain.model.aggregates.Clinic;
import com.nexoshift.petcarehub.profiles.domain.model.aggregates.ClinicPatient;
import com.nexoshift.petcarehub.profiles.domain.model.aggregates.Pet;
import com.nexoshift.petcarehub.profiles.domain.model.queries.*;
import com.nexoshift.petcarehub.profiles.domain.model.valueobjects.ClinicId;
import com.nexoshift.petcarehub.profiles.domain.model.valueobjects.PetId;
import com.nexoshift.petcarehub.profiles.domain.services.ClinicPatientQueryService;
import com.nexoshift.petcarehub.profiles.infrastructure.persistence.jpa.repositories.ClinicPatientRepository;
import com.nexoshift.petcarehub.profiles.infrastructure.persistence.jpa.repositories.ClinicRepository;
import com.nexoshift.petcarehub.profiles.infrastructure.persistence.jpa.repositories.PetOwnerRepository;
import com.nexoshift.petcarehub.profiles.infrastructure.persistence.jpa.repositories.PetRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Implementation of the clinic patient query service.
 */
@Service
@Transactional(readOnly = true)
public class ClinicPatientQueryServiceImpl implements ClinicPatientQueryService {

    private final ClinicPatientRepository clinicPatientRepository;
    private final ClinicRepository clinicRepository;
    private final PetRepository petRepository;
    private final IamExternalService iamExternalService;
    private final PetOwnerRepository petOwnerRepository;

    /**
     * Constructs the clinic patient query service.
     *
     * @param clinicPatientRepository the clinic patient repository
     * @param clinicRepository the clinic repository
     * @param petRepository the pet repository
     * @param iamExternalService the IAM external service
     * @param petOwnerRepository the pet owner repository
     */
    public ClinicPatientQueryServiceImpl(
            ClinicPatientRepository clinicPatientRepository,
            ClinicRepository clinicRepository,
            PetRepository petRepository,
            IamExternalService iamExternalService,
            PetOwnerRepository petOwnerRepository) {

        this.clinicPatientRepository = clinicPatientRepository;
        this.clinicRepository = clinicRepository;
        this.petRepository = petRepository;
        this.iamExternalService = iamExternalService;
        this.petOwnerRepository = petOwnerRepository;
    }

    /**
     * Retrieves a clinic patient by its identifier.
     *
     * @param query the clinic patient identifier query
     * @return the clinic patient, if found
     */
    @Override
    public Optional<ClinicPatient> handle(GetClinicPatientByIdQuery query) {

        var clinicPatient = clinicPatientRepository.findById(
                query.clinicPatientId());

        if (clinicPatient.isEmpty()) {
            return Optional.empty();
        }

        var authenticatedUserId = iamExternalService.fetchAuthenticatedUserId();

        var clinicId = clinicPatient.get().getClinicId().clinicId();
        var petId = clinicPatient.get().getPetId().petId();

        var clinic = clinicRepository.findById(clinicId)
                .orElseThrow(() -> new ClinicNotFoundException(clinicId));

        var isClinicStaff = iamExternalService.userHasRole(
                authenticatedUserId, "ROLE_CLINIC_ADMIN")
                || iamExternalService.userHasRole(
                authenticatedUserId, "ROLE_VETERINARIAN");

        var accountId = iamExternalService.fetchAccountIdByUserId(
                authenticatedUserId);

        if (isClinicStaff
                && clinic.getAccountId().accountId().equals(accountId)) {
            return clinicPatient;
        }

        validatePetOwner(petId);

        return clinicPatient;

    }

    /**
     * Retrieves pets registered as patients of a clinic.
     *
     * @param query the clinic identifier query
     * @return the list of registered pets
     */
    @Override
    public List<Pet> handle(GetPatientPetsByClinicIdQuery query) {

        validateClinicStaff(query.clinicId());

        var clinicPatients = clinicPatientRepository.findByClinicId(
                new ClinicId(query.clinicId())
        );

        var petIds = clinicPatients.stream()
                .map(clinicPatient -> clinicPatient.getPetId().petId())
                .toList();

        return petRepository.findAllById(petIds);
    }

    /**
     * Retrieves clinics where a pet is registered as a patient.
     *
     * @param query the pet identifier query
     * @return the list of associated clinics
     */
    @Override
    public List<Clinic> handle(GetPatientClinicsByPetIdQuery query) {

        validatePetOwner(query.petId());

        var clinicPatients = clinicPatientRepository.findByPetId(
                new PetId(query.petId())
        );

        var clinicIds = clinicPatients.stream()
                .map(clinicPatient -> clinicPatient.getClinicId().clinicId())
                .toList();

        return clinicRepository.findAllById(clinicIds);
    }

    /**
     * Validates that the authenticated user belongs to the clinic staff.
     *
     * @param clinicId the clinic identifier
     */
    private void validateClinicStaff(Long clinicId) {

        var clinic = clinicRepository.findById(clinicId)
                .orElseThrow(() -> new ClinicNotFoundException(clinicId));

        var authenticatedUserId = iamExternalService.fetchAuthenticatedUserId();

        var isClinicAdmin = iamExternalService.userHasRole(
                authenticatedUserId, "ROLE_CLINIC_ADMIN");

        var isVeterinarian = iamExternalService.userHasRole(
                authenticatedUserId, "ROLE_VETERINARIAN");

        var accountId = iamExternalService.fetchAccountIdByUserId(
                authenticatedUserId);

        if ((!isClinicAdmin && !isVeterinarian)
                || !clinic.getAccountId().accountId().equals(accountId)) {
            throw new ProfileOperationNotAllowedException(
                    "You are not authorized to access this clinic's patients");
        }
    }

    /**
     * Validates that the authenticated user owns the pet.
     *
     * @param petId the pet identifier
     */
    private void validatePetOwner(Long petId) {

        var pet = petRepository.findById(petId)
                .orElseThrow(() -> new PetNotFoundException(petId));

        var ownerId = pet.getOwnerId().ownerId();

        var petOwner = petOwnerRepository.findById(ownerId)
                .orElseThrow(() -> new PetOwnerNotFoundException(ownerId));

        var authenticatedUserId = iamExternalService.fetchAuthenticatedUserId();

        if (!petOwner.getUserId().userId().equals(authenticatedUserId)) {
            throw new ProfileOperationNotAllowedException(
                    "You are not authorized to access this pet's clinics");
        }
    }
}
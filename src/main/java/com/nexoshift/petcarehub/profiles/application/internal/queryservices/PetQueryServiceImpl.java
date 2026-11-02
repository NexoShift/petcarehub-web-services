package com.nexoshift.petcarehub.profiles.application.internal.queryservices;

import com.nexoshift.petcarehub.profiles.application.internal.outboundservices.acl.IamExternalService;
import com.nexoshift.petcarehub.profiles.domain.exceptions.PetOwnerNotFoundException;
import com.nexoshift.petcarehub.profiles.domain.exceptions.ProfileOperationNotAllowedException;
import com.nexoshift.petcarehub.profiles.domain.model.aggregates.Pet;
import com.nexoshift.petcarehub.profiles.domain.model.queries.GetPetByCodeQuery;
import com.nexoshift.petcarehub.profiles.domain.model.queries.GetPetByIdQuery;
import com.nexoshift.petcarehub.profiles.domain.model.queries.GetPetsByOwnerIdQuery;
import com.nexoshift.petcarehub.profiles.domain.model.valueobjects.OwnerId;
import com.nexoshift.petcarehub.profiles.domain.model.valueobjects.PetCode;
import com.nexoshift.petcarehub.profiles.domain.model.valueobjects.PetId;
import com.nexoshift.petcarehub.profiles.domain.services.PetQueryService;
import com.nexoshift.petcarehub.profiles.infrastructure.persistence.jpa.repositories.ClinicPatientRepository;
import com.nexoshift.petcarehub.profiles.infrastructure.persistence.jpa.repositories.ClinicRepository;
import com.nexoshift.petcarehub.profiles.infrastructure.persistence.jpa.repositories.PetOwnerRepository;
import com.nexoshift.petcarehub.profiles.infrastructure.persistence.jpa.repositories.PetRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Implementation of the pet query service.
 * Handles queries related to pet profiles.
 *
 * @see PetQueryService
 * @see PetRepository
 */
@Service
public class PetQueryServiceImpl implements PetQueryService {

    private final PetRepository petRepository;
    private final PetOwnerRepository petOwnerRepository;
    private final IamExternalService iamExternalService;
    private final ClinicPatientRepository clinicPatientRepository;
    private final ClinicRepository clinicRepository;

    /**
     * Constructs a pet query service.
     *
     * @param petRepository the pet repository
     * @param petOwnerRepository the pet owner repository
     * @param iamExternalService the IAM external service
     * @param clinicPatientRepository the clinic patient repository
     * @param clinicRepository the clinic repository
     */
    public PetQueryServiceImpl(
            PetRepository petRepository,
            PetOwnerRepository petOwnerRepository,
            IamExternalService iamExternalService,
            ClinicPatientRepository clinicPatientRepository,
            ClinicRepository clinicRepository) {
        this.petRepository = petRepository;
        this.petOwnerRepository = petOwnerRepository;
        this.iamExternalService = iamExternalService;
        this.clinicPatientRepository = clinicPatientRepository;
        this.clinicRepository = clinicRepository;
    }

    /**
     * Retrieves a pet by its profile identifier.
     *
     * @param query the pet identifier query
     * @return the pet, if found
     */
    @Override
    public Optional<Pet> handle(GetPetByIdQuery query) {

        var pet = petRepository.findById(query.petId());

        if (pet.isEmpty()) return Optional.empty();

        if (!canAccessPet(pet.get())) {
            throw new ProfileOperationNotAllowedException(
                    "You are not authorized to access this pet");
        }

        return pet;
    }

    /**
     * Retrieves pets associated with an owner.
     *
     * @param query the owner identifier query
     * @return the list of pets
     */
    @Override
    public List<Pet> handle(GetPetsByOwnerIdQuery query) {

        var petOwner = petOwnerRepository.findById(query.ownerId());

        if (petOwner.isEmpty()) {
            throw new PetOwnerNotFoundException(query.ownerId());
        }

        var authenticatedUserId = iamExternalService.fetchAuthenticatedUserId();

        if (authenticatedUserId == null || authenticatedUserId <= 0
                || !iamExternalService.existsActiveUser(authenticatedUserId)) {
            throw new ProfileOperationNotAllowedException(
                    "You are not authorized to access these pets");
        }

        var pets = petRepository.findByOwnerId(
                new OwnerId(query.ownerId()));

        if (petOwner.get().getUserId().userId()
                .equals(authenticatedUserId)) {
            return pets;
        }

        var isClinicAdmin = iamExternalService.userHasRole(
                authenticatedUserId, "ROLE_CLINIC_ADMIN");

        var isVeterinarian = iamExternalService.userHasRole(
                authenticatedUserId, "ROLE_VETERINARIAN");

        if (!isClinicAdmin && !isVeterinarian) {
            throw new ProfileOperationNotAllowedException(
                    "You are not authorized to access these pets");
        }

        var hasPatientInClinic = pets.stream().anyMatch(this::canAccessPet);

        if (!hasPatientInClinic) {
            throw new ProfileOperationNotAllowedException(
                    "You are not authorized to access these pets");
        }

        return pets;
    }

    @Override
    public Optional<Pet> handle(GetPetByCodeQuery query) {
        var authenticatedUserId = iamExternalService.fetchAuthenticatedUserId();

        var isClinicAdmin = iamExternalService.userHasRole(
                authenticatedUserId, "ROLE_CLINIC_ADMIN");

        var isVeterinarian = iamExternalService.userHasRole(
                authenticatedUserId, "ROLE_VETERINARIAN");

        if (!isClinicAdmin && !isVeterinarian) {
            throw new ProfileOperationNotAllowedException(
                    "You are not authorized to search patients");
        }

        var petCode = new PetCode(query.petCode());

        return petRepository.findByPetCode(petCode);
    }



    /**
     * Checks whether the authenticated user can access a pet profile.
     *
     * @param pet the pet to validate
     * @return true if the user has access, false otherwise
     */
    private boolean canAccessPet(Pet pet) {

        var authenticatedUserId = iamExternalService.fetchAuthenticatedUserId();

        if (authenticatedUserId == null || authenticatedUserId <= 0
                || !iamExternalService.existsActiveUser(authenticatedUserId)) {
            return false;
        }

        var ownerId = pet.getOwnerId().ownerId();

        var petOwner = petOwnerRepository.findById(ownerId);

        if (petOwner.isPresent()
                && petOwner.get().getUserId().userId()
                .equals(authenticatedUserId)) {
            return true;
        }

        var isClinicAdmin = iamExternalService.userHasRole(
                authenticatedUserId, "ROLE_CLINIC_ADMIN");

        var isVeterinarian = iamExternalService.userHasRole(
                authenticatedUserId, "ROLE_VETERINARIAN");

        if (!isClinicAdmin && !isVeterinarian) {
            return false;
        }

        var accountId = iamExternalService.fetchAccountIdByUserId(
                authenticatedUserId);

        var clinicPatients = clinicPatientRepository.findByPetId(
                new PetId(pet.getId()));

        for (var clinicPatient : clinicPatients) {

            var clinicId = clinicPatient.getClinicId().clinicId();

            var clinic = clinicRepository.findById(clinicId);

            if (clinic.isPresent()
                    && clinic.get().getAccountId().accountId()
                    .equals(accountId)) {
                return true;
            }
        }

        return false;
    }
}

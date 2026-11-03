package com.nexoshift.petcarehub.profiles.application.internal.queryservices;

import com.nexoshift.petcarehub.profiles.application.internal.outboundservices.acl.IamExternalService;
import com.nexoshift.petcarehub.profiles.domain.exceptions.ProfileOperationNotAllowedException;
import com.nexoshift.petcarehub.profiles.domain.model.aggregates.Pet;
import com.nexoshift.petcarehub.profiles.domain.model.aggregates.PetOwner;
import com.nexoshift.petcarehub.profiles.domain.model.queries.GetPetOwnerByIdQuery;
import com.nexoshift.petcarehub.profiles.domain.model.queries.GetPetOwnerByUserIdQuery;
import com.nexoshift.petcarehub.profiles.domain.model.valueobjects.OwnerId;
import com.nexoshift.petcarehub.profiles.domain.model.valueobjects.PetId;
import com.nexoshift.petcarehub.profiles.domain.model.valueobjects.UserId;
import com.nexoshift.petcarehub.profiles.domain.services.PetOwnerQueryService;
import com.nexoshift.petcarehub.profiles.infrastructure.persistence.jpa.repositories.ClinicPatientRepository;
import com.nexoshift.petcarehub.profiles.infrastructure.persistence.jpa.repositories.ClinicRepository;
import com.nexoshift.petcarehub.profiles.infrastructure.persistence.jpa.repositories.PetOwnerRepository;
import com.nexoshift.petcarehub.profiles.infrastructure.persistence.jpa.repositories.PetRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Implementation of the pet owner query service.
 * Handles queries related to pet owner profiles.
 *
 * @see PetOwnerQueryService
 * @see PetOwnerRepository
 */
@Service
public class PetOwnerQueryServiceImpl implements PetOwnerQueryService {

    private final PetOwnerRepository petOwnerRepository;
    private final IamExternalService iamExternalService;
    private final PetRepository petRepository;
    private final ClinicPatientRepository clinicPatientRepository;
    private final ClinicRepository clinicRepository;

    /**
     * Constructs a pet owner query service.
     *
     * @param petOwnerRepository the pet owner repository
     * @param iamExternalService the IAM external service
     * @param petRepository the pet repository
     * @param clinicPatientRepository the clinic patient repository
     * @param clinicRepository the clinic repository
     */
    public PetOwnerQueryServiceImpl(
            PetOwnerRepository petOwnerRepository,
            IamExternalService iamExternalService,
            PetRepository petRepository,
            ClinicPatientRepository clinicPatientRepository,
            ClinicRepository clinicRepository) {
        this.petOwnerRepository = petOwnerRepository;
        this.iamExternalService = iamExternalService;
        this.petRepository = petRepository;
        this.clinicPatientRepository = clinicPatientRepository;
        this.clinicRepository = clinicRepository;
    }

    /**
     * Retrieves a pet owner by its profile identifier.
     *
     * @param query the pet owner identifier query
     * @return the pet owner, if found
     */
    @Override
    public Optional<PetOwner> handle(GetPetOwnerByIdQuery query) {

        var petOwner = petOwnerRepository.findById(query.petOwnerId());

        if (petOwner.isEmpty()) return Optional.empty();

        if (!canAccessPetOwner(petOwner.get())) {
            throw new ProfileOperationNotAllowedException(
                    "You are not authorized to access this pet owner");
        }

        return petOwner;
    }

    /**
     * Retrieves a pet owner by its IAM user identifier.
     *
     * @param query the IAM user identifier query
     * @return the pet owner, if found
     */
    @Override
    public Optional<PetOwner> handle(GetPetOwnerByUserIdQuery query) {
        var petOwner = petOwnerRepository.findByUserId(
                new UserId(query.userId()));

        if (petOwner.isEmpty()) {
            return Optional.empty();
        }

        if (!canAccessPetOwner(petOwner.get())) {
            throw new ProfileOperationNotAllowedException(
                    "You are not authorized to access this pet owner");
        }

        return petOwner;
    }

    /**
     * Checks whether the authenticated user can access a pet owner profile.
     *
     * @param petOwner the pet owner to validate
     * @return true if the user has access, false otherwise
     */
    private boolean canAccessPetOwner(PetOwner petOwner) {

        var authenticatedUserId = iamExternalService.fetchAuthenticatedUserId();

        if (authenticatedUserId == null || authenticatedUserId <= 0
                || !iamExternalService.existsActiveUser(authenticatedUserId)) {
            return false;
        }

        if (petOwner.getUserId().userId().equals(authenticatedUserId)) {
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

        var pets = petRepository.findByOwnerId(
                new OwnerId(petOwner.getId()));

        for (var pet : pets) {

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
        }

        return false;
    }
}

package com.nexoshift.petcarehub.profiles.application.internal.commandservices;

import com.nexoshift.petcarehub.profiles.application.internal.outboundservices.acl.IamExternalService;
import com.nexoshift.petcarehub.profiles.domain.exceptions.ClinicNotFoundException;
import com.nexoshift.petcarehub.profiles.domain.exceptions.PetNotFoundException;
import com.nexoshift.petcarehub.profiles.domain.exceptions.ProfileOperationNotAllowedException;
import com.nexoshift.petcarehub.profiles.domain.model.aggregates.HistoryAccessGrant;
import com.nexoshift.petcarehub.profiles.domain.model.commands.GrantHistoryAccessCommand;
import com.nexoshift.petcarehub.profiles.domain.model.commands.RevokeHistoryAccessCommand;
import com.nexoshift.petcarehub.profiles.domain.model.valueobjects.ClinicId;
import com.nexoshift.petcarehub.profiles.domain.model.valueobjects.PetId;
import com.nexoshift.petcarehub.profiles.domain.services.HistoryAccessGrantCommandService;
import com.nexoshift.petcarehub.profiles.infrastructure.persistence.jpa.repositories.ClinicPatientRepository;
import com.nexoshift.petcarehub.profiles.infrastructure.persistence.jpa.repositories.ClinicRepository;
import com.nexoshift.petcarehub.profiles.infrastructure.persistence.jpa.repositories.HistoryAccessGrantRepository;
import com.nexoshift.petcarehub.profiles.infrastructure.persistence.jpa.repositories.PetOwnerRepository;
import com.nexoshift.petcarehub.profiles.infrastructure.persistence.jpa.repositories.PetRepository;
import com.nexoshift.petcarehub.shared.domain.exceptions.ConflictException;
import com.nexoshift.petcarehub.shared.domain.exceptions.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

/**
 * Only the owner of the pet gives or revokes access to the history.
 */
@Service
public class HistoryAccessGrantCommandServiceImpl implements HistoryAccessGrantCommandService {

    private final HistoryAccessGrantRepository grantRepository;
    private final PetRepository petRepository;
    private final PetOwnerRepository petOwnerRepository;
    private final ClinicRepository clinicRepository;
    private final ClinicPatientRepository clinicPatientRepository;
    private final IamExternalService iamExternalService;

    public HistoryAccessGrantCommandServiceImpl(
            HistoryAccessGrantRepository grantRepository,
            PetRepository petRepository,
            PetOwnerRepository petOwnerRepository,
            ClinicRepository clinicRepository,
            ClinicPatientRepository clinicPatientRepository,
            IamExternalService iamExternalService) {
        this.grantRepository = grantRepository;
        this.petRepository = petRepository;
        this.petOwnerRepository = petOwnerRepository;
        this.clinicRepository = clinicRepository;
        this.clinicPatientRepository = clinicPatientRepository;
        this.iamExternalService = iamExternalService;
    }

    /**
     * Gives (or renews) the access of a clinic where the pet is a patient.
     * Giving it again to the same clinic starts a new period from now.
     */
    @Override
    @Transactional
    public HistoryAccessGrant handle(GrantHistoryAccessCommand command) {
        requireOwner(command.petId());
        if (!clinicRepository.existsById(command.clinicId())) {
            throw new ClinicNotFoundException(command.clinicId());
        }
        var petId = new PetId(command.petId());
        var clinicId = new ClinicId(command.clinicId());
        if (!clinicPatientRepository.existsByClinicIdAndPetId(clinicId, petId)) {
            throw new ConflictException(
                    "The pet is not a patient of this clinic: share the pet code with it first");
        }
        var now = Instant.now();
        var grant = grantRepository.findByPetIdAndClinicId(petId, clinicId)
                .map(existing -> {
                    existing.renew(command.hours(), now);
                    return existing;
                })
                .orElseGet(() -> new HistoryAccessGrant(command, now));
        return grantRepository.save(grant);
    }

    @Override
    @Transactional
    public HistoryAccessGrant handle(RevokeHistoryAccessCommand command) {
        requireOwner(command.petId());
        var grant = grantRepository.findById(command.grantId())
                .filter(found -> found.getPetId().petId().equals(command.petId()))
                .orElseThrow(() -> new NotFoundException(
                        "History access grant " + command.grantId() + " was not found"));
        grant.revoke(Instant.now());
        return grantRepository.save(grant);
    }

    private void requireOwner(Long petId) {
        var pet = petRepository.findById(petId)
                .orElseThrow(() -> new PetNotFoundException(petId));
        var ownerUserId = petOwnerRepository.findById(pet.getOwnerId().ownerId())
                .map(owner -> owner.getUserId().userId());
        var authenticatedUserId = iamExternalService.fetchAuthenticatedUserId();
        if (ownerUserId.isEmpty() || !ownerUserId.get().equals(authenticatedUserId)) {
            throw new ProfileOperationNotAllowedException(
                    "Only the owner of the pet can give or revoke access to its history");
        }
    }
}

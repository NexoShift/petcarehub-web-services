package com.nexoshift.petcarehub.profiles.application.internal.queryservices;

import com.nexoshift.petcarehub.profiles.application.internal.outboundservices.acl.IamExternalService;
import com.nexoshift.petcarehub.profiles.domain.exceptions.PetNotFoundException;
import com.nexoshift.petcarehub.profiles.domain.exceptions.ProfileOperationNotAllowedException;
import com.nexoshift.petcarehub.profiles.domain.model.aggregates.HistoryAccessGrant;
import com.nexoshift.petcarehub.profiles.domain.model.queries.GetHistoryAccessGrantsByPetIdQuery;
import com.nexoshift.petcarehub.profiles.domain.model.valueobjects.ClinicId;
import com.nexoshift.petcarehub.profiles.domain.model.valueobjects.PetId;
import com.nexoshift.petcarehub.profiles.domain.model.valueobjects.UserId;
import com.nexoshift.petcarehub.profiles.domain.services.HistoryAccessGrantQueryService;
import com.nexoshift.petcarehub.profiles.infrastructure.persistence.jpa.repositories.HistoryAccessGrantRepository;
import com.nexoshift.petcarehub.profiles.infrastructure.persistence.jpa.repositories.PetOwnerRepository;
import com.nexoshift.petcarehub.profiles.infrastructure.persistence.jpa.repositories.PetRepository;
import com.nexoshift.petcarehub.profiles.infrastructure.persistence.jpa.repositories.VeterinarianRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
public class HistoryAccessGrantQueryServiceImpl implements HistoryAccessGrantQueryService {

    private final HistoryAccessGrantRepository grantRepository;
    private final PetRepository petRepository;
    private final PetOwnerRepository petOwnerRepository;
    private final VeterinarianRepository veterinarianRepository;
    private final IamExternalService iamExternalService;

    public HistoryAccessGrantQueryServiceImpl(
            HistoryAccessGrantRepository grantRepository,
            PetRepository petRepository,
            PetOwnerRepository petOwnerRepository,
            VeterinarianRepository veterinarianRepository,
            IamExternalService iamExternalService) {
        this.grantRepository = grantRepository;
        this.petRepository = petRepository;
        this.petOwnerRepository = petOwnerRepository;
        this.veterinarianRepository = veterinarianRepository;
        this.iamExternalService = iamExternalService;
    }

    /** The owner sees every grant; a veterinarian sees only the grant of their clinic. */
    @Override
    @Transactional(readOnly = true)
    public List<HistoryAccessGrant> handle(GetHistoryAccessGrantsByPetIdQuery query) {
        var pet = petRepository.findById(query.petId())
                .orElseThrow(() -> new PetNotFoundException(query.petId()));
        var authenticatedUserId = iamExternalService.fetchAuthenticatedUserId();
        var grants = grantRepository.findAllByPetIdOrderByGrantedAtDesc(new PetId(query.petId()));

        var isOwner = petOwnerRepository.findById(pet.getOwnerId().ownerId())
                .map(owner -> owner.getUserId().userId().equals(authenticatedUserId))
                .orElse(false);
        if (isOwner) {
            return grants;
        }
        var clinicId = veterinarianRepository.findByUserId(new UserId(authenticatedUserId))
                .map(veterinarian -> veterinarian.getClinicId())
                .orElseThrow(() -> new ProfileOperationNotAllowedException(
                        "Only the owner or a veterinarian can see the access grants"));
        return grants.stream()
                .filter(grant -> grant.getClinicId().equals(clinicId))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasActiveAccess(Long clinicId, Long petId) {
        if (clinicId == null || clinicId <= 0 || petId == null || petId <= 0) {
            return false;
        }
        return grantRepository.findByPetIdAndClinicId(new PetId(petId), new ClinicId(clinicId))
                .map(grant -> grant.isActiveAt(Instant.now()))
                .orElse(false);
    }
}

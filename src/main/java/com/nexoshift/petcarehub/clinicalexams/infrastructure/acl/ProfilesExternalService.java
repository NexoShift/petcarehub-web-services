package com.nexoshift.petcarehub.clinicalexams.infrastructure.acl;

import com.nexoshift.petcarehub.clinicalexams.application.internal.outboundservices.acl.ExternalProfilesService;
import com.nexoshift.petcarehub.profiles.interfaces.acl.ProfileContextFacade;
import org.springframework.stereotype.Service;

import java.util.Optional;

/** Adapter to the published facade of Profiles (0 means "does not exist"). */
@Service("clinicalExamsProfilesExternalService")
public class ProfilesExternalService implements ExternalProfilesService {

    private final ProfileContextFacade profileContextFacade;

    public ProfilesExternalService(ProfileContextFacade profileContextFacade) {
        this.profileContextFacade = profileContextFacade;
    }

    @Override
    public Optional<Long> fetchVeterinarianIdByUserId(Long userId) {
        return positive(profileContextFacade.fetchVeterinarianIdByUserId(userId));
    }

    @Override
    public Optional<Long> fetchClinicIdByVeterinarianId(Long veterinarianId) {
        return positive(profileContextFacade.fetchClinicIdByVeterinarianId(veterinarianId));
    }

    @Override
    public Optional<Long> fetchOwnerUserIdByPetId(Long petId) {
        return positive(profileContextFacade.fetchPetOwnerIdByPetId(petId))
                .flatMap(ownerId -> positive(profileContextFacade.fetchUserIdByPetOwnerId(ownerId)));
    }

    @Override
    public boolean isPetPatientOfClinic(Long clinicId, Long petId) {
        return profileContextFacade.isPetPatientOfClinic(clinicId, petId);
    }

    private static Optional<Long> positive(Long value) {
        return value != null && value > 0 ? Optional.of(value) : Optional.empty();
    }
}

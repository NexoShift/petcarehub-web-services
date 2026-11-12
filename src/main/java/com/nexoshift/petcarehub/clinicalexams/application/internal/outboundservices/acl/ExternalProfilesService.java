package com.nexoshift.petcarehub.clinicalexams.application.internal.outboundservices.acl;

import java.util.Optional;

/** Outbound port to Profiles: veterinarians, clinics and pet owners. */
public interface ExternalProfilesService {

    Optional<Long> fetchVeterinarianIdByUserId(Long userId);

    Optional<Long> fetchClinicIdByVeterinarianId(Long veterinarianId);

    /** IAM user id of the owner of the pet. */
    Optional<Long> fetchOwnerUserIdByPetId(Long petId);

    boolean isPetPatientOfClinic(Long clinicId, Long petId);
}

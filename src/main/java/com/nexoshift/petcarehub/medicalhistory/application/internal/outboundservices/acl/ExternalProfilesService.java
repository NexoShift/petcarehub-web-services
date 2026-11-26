package com.nexoshift.petcarehub.medicalhistory.application.internal.outboundservices.acl;

import java.util.Optional;

/** Outbound port to Profiles: pets, owners, veterinarians and patients of a clinic. */
public interface ExternalProfilesService {

  boolean existsPet(Long petId);

  /** IAM user id of the owner of the pet. */
  Optional<Long> fetchOwnerUserIdByPetId(Long petId);

  Optional<Long> fetchVeterinarianIdByUserId(Long userId);

  Optional<Long> fetchClinicIdByVeterinarianId(Long veterinarianId);

  boolean isPetPatientOfClinic(Long clinicId, Long petId);
}

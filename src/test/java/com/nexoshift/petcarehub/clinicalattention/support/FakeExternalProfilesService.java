package com.nexoshift.petcarehub.clinicalattention.support;

import com.nexoshift.petcarehub.clinicalattention.application.internal.outboundservices.acl.ExternalProfilesService;
import com.nexoshift.petcarehub.clinicalattention.domain.model.valueobjects.ClinicId;
import com.nexoshift.petcarehub.clinicalattention.domain.model.valueobjects.ConsultingRoomId;
import com.nexoshift.petcarehub.clinicalattention.domain.model.valueobjects.PetId;
import com.nexoshift.petcarehub.clinicalattention.domain.model.valueobjects.VeterinarianId;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/** Profiles &amp; Organization as seen from the tests: only what was configured exists. */
public class FakeExternalProfilesService implements ExternalProfilesService {

  private final Set<Long> pets = new HashSet<>();
  private final Map<Long, Long> petOwnerUserIds = new HashMap<>();
  private final Map<Long, Long> veterinarianIdsByUserId = new HashMap<>();
  private final Set<String> activeVeterinarians = new HashSet<>();
  private final Set<String> activeRooms = new HashSet<>();

  public FakeExternalProfilesService withPet(long petId, long ownerUserId) {
    pets.add(petId);
    petOwnerUserIds.put(petId, ownerUserId);
    return this;
  }

  public FakeExternalProfilesService withVeterinarian(long userId, long veterinarianId) {
    veterinarianIdsByUserId.put(userId, veterinarianId);
    return this;
  }

  public FakeExternalProfilesService withVeterinarianInClinic(
      long veterinarianId, long clinicId) {
    activeVeterinarians.add(veterinarianId + ":" + clinicId);
    return this;
  }

  public FakeExternalProfilesService withConsultingRoom(long clinicId, long consultingRoomId) {
    activeRooms.add(clinicId + ":" + consultingRoomId);
    return this;
  }

  @Override
  public boolean existsPet(PetId petId) {
    return pets.contains(petId.petId());
  }

  @Override
  public Optional<VeterinarianId> fetchVeterinarianIdByUserId(Long userId) {
    return Optional.ofNullable(veterinarianIdsByUserId.get(userId)).map(VeterinarianId::new);
  }

  @Override
  public boolean isVeterinarianActiveInClinic(VeterinarianId veterinarianId, ClinicId clinicId) {
    return activeVeterinarians.contains(veterinarianId.veterinarianId() + ":" + clinicId.clinicId());
  }

  @Override
  public boolean isConsultingRoomActive(ClinicId clinicId, ConsultingRoomId consultingRoomId) {
    return activeRooms.contains(clinicId.clinicId() + ":" + consultingRoomId.consultingRoomId());
  }

  @Override
  public Optional<Long> fetchPetOwnerUserIdByPetId(PetId petId) {
    return Optional.ofNullable(petOwnerUserIds.get(petId.petId()));
  }
}

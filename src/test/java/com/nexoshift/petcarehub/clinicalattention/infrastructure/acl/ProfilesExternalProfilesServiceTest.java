package com.nexoshift.petcarehub.clinicalattention.infrastructure.acl;

import com.nexoshift.petcarehub.clinicalattention.domain.model.valueobjects.ClinicId;
import com.nexoshift.petcarehub.clinicalattention.domain.model.valueobjects.ConsultingRoomId;
import com.nexoshift.petcarehub.clinicalattention.domain.model.valueobjects.PetId;
import com.nexoshift.petcarehub.clinicalattention.domain.model.valueobjects.VeterinarianId;
import com.nexoshift.petcarehub.profiles.interfaces.acl.ProfileContextFacade;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * NUEVO. El adaptador traduce las respuestas de Profiles (Long, 0L = no existe) a los
 * value objects de Clinical Attention. La fachada de Profiles se simula con Mockito:
 * lo no configurado responde 0L / false, igual que la fachada real cuando no encuentra algo.
 */
class ProfilesExternalProfilesServiceTest {

  private ProfileContextFacade facade;
  private ProfilesExternalProfilesService adapter;

  @BeforeEach
  void setUp() {
    facade = mock(ProfileContextFacade.class);
    adapter = new ProfilesExternalProfilesService(facade);
  }

  @Test
  @DisplayName("userId of IAM -> id of the veterinarian profile; 0 means not a veterinarian")
  void veterinarianIdByUserId() {
    when(facade.fetchVeterinarianIdByUserId(10L)).thenReturn(100L);
    when(facade.fetchVeterinarianIdByUserId(11L)).thenReturn(0L);

    assertThat(adapter.fetchVeterinarianIdByUserId(10L)).contains(new VeterinarianId(100L));
    assertThat(adapter.fetchVeterinarianIdByUserId(11L)).isEmpty();
  }

  @Test
  @DisplayName("The veterinarian is active in a clinic only when the profile belongs to it")
  void veterinarianInClinic() {
    when(facade.fetchClinicIdByVeterinarianId(100L)).thenReturn(3L);

    assertThat(adapter.isVeterinarianActiveInClinic(new VeterinarianId(100L), new ClinicId(3L)))
        .isTrue();
    assertThat(adapter.isVeterinarianActiveInClinic(new VeterinarianId(100L), new ClinicId(4L)))
        .isFalse();
    assertThat(adapter.isVeterinarianActiveInClinic(new VeterinarianId(101L), new ClinicId(3L)))
        .isFalse();
  }

  @Test
  @DisplayName("Owner of the pet: pet -> owner profile -> IAM user")
  void ownerUserId() {
    when(facade.fetchPetOwnerIdByPetId(5L)).thenReturn(8L);
    when(facade.fetchUserIdByPetOwnerId(8L)).thenReturn(20L);

    assertThat(adapter.fetchPetOwnerUserIdByPetId(new PetId(5L))).contains(20L);
    assertThat(adapter.fetchPetOwnerUserIdByPetId(new PetId(6L))).isEmpty();
  }

  @Test
  @DisplayName("Pet, patient and consulting room questions go to the facade with the right order")
  void delegates() {
    when(facade.existsPet(5L)).thenReturn(true);
    when(facade.isPetPatientOfClinic(3L, 5L)).thenReturn(true);   // ojo: (clinicId, petId)
    when(facade.existsActiveConsultingRoom(3L, 7L)).thenReturn(true);

    assertThat(adapter.existsPet(new PetId(5L))).isTrue();
    assertThat(adapter.existsPet(new PetId(6L))).isFalse();
    assertThat(adapter.isPetPatientOfClinic(new PetId(5L), new ClinicId(3L))).isTrue();
    assertThat(adapter.isPetPatientOfClinic(new PetId(5L), new ClinicId(4L))).isFalse();
    assertThat(adapter.isConsultingRoomActive(new ClinicId(3L), new ConsultingRoomId(7L)))
        .isTrue();
    assertThat(adapter.fetchClinicIdByVeterinarianId(new VeterinarianId(100L))).isEmpty();
  }
}

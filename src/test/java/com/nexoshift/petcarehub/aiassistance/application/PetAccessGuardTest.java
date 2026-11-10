package com.nexoshift.petcarehub.aiassistance.application;

import com.nexoshift.petcarehub.aiassistance.application.internal.access.PetAccessGuard;
import com.nexoshift.petcarehub.aiassistance.application.internal.outboundservices.acl.ExternalProfilesService;
import com.nexoshift.petcarehub.aiassistance.domain.model.valueobjects.RequesterRole;
import com.nexoshift.petcarehub.shared.domain.exceptions.ForbiddenException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/** NUEVO. Regla de acceso de la IA a una mascota (sin Spring ni base de datos). */
class PetAccessGuardTest {

  private static final long PET_ID = 5L;
  private static final long OWNER_USER_ID = 20L;
  private static final long VET_USER_ID = 10L;
  private static final long OTHER_USER_ID = 99L;

  private PetAccessGuard guard;

  @BeforeEach
  void setUp() {
    var profiles = mock(ExternalProfilesService.class);   // todo lo no configurado responde false
    when(profiles.isPetOwnedBy(PET_ID, OWNER_USER_ID)).thenReturn(true);
    when(profiles.isPetPatientOfVeterinarianClinic(PET_ID, VET_USER_ID)).thenReturn(true);
    guard = new PetAccessGuard(profiles);
  }

  @Test
  @DisplayName("The owner can use the AI with their pet")
  void ownerAllowed() {
    assertThatCode(() -> guard.ensureCanAccessPet(PET_ID, OWNER_USER_ID, RequesterRole.PET_OWNER))
        .doesNotThrowAnyException();
  }

  @Test
  @DisplayName("Another owner cannot")
  void otherOwnerForbidden() {
    assertThatThrownBy(
        () -> guard.ensureCanAccessPet(PET_ID, OTHER_USER_ID, RequesterRole.PET_OWNER))
        .isInstanceOf(ForbiddenException.class);
  }

  @Test
  @DisplayName("A vet of a clinic where the pet is a patient can")
  void veterinarianOfClinicAllowed() {
    assertThatCode(
        () -> guard.ensureCanAccessPet(PET_ID, VET_USER_ID, RequesterRole.VETERINARIAN))
        .doesNotThrowAnyException();
  }

  @Test
  @DisplayName("A vet of another clinic cannot")
  void veterinarianOfOtherClinicForbidden() {
    assertThatThrownBy(
        () -> guard.ensureCanAccessPet(PET_ID, OTHER_USER_ID, RequesterRole.VETERINARIAN))
        .isInstanceOf(ForbiddenException.class);
  }
}

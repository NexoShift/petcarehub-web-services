package com.nexoshift.petcarehub.clinicalattention.application;

import com.nexoshift.petcarehub.clinicalattention.application.internal.queryservices.ClinicalConsultationQueryServiceImpl;
import com.nexoshift.petcarehub.clinicalattention.domain.exceptions.PetNotFoundException;
import com.nexoshift.petcarehub.clinicalattention.domain.model.aggregates.ClinicalConsultation;
import com.nexoshift.petcarehub.clinicalattention.domain.model.commands.CreateClinicalConsultationCommand;
import com.nexoshift.petcarehub.clinicalattention.domain.model.queries.GetAuthorizedClinicalConsultationByIdQuery;
import com.nexoshift.petcarehub.clinicalattention.domain.model.queries.GetClinicalConsultationByIdQuery;
import com.nexoshift.petcarehub.clinicalattention.domain.model.queries.GetClinicalConsultationsByPetIdQuery;
import com.nexoshift.petcarehub.clinicalattention.domain.model.queries.GetClinicalConsultationsByVeterinarianIdQuery;
import com.nexoshift.petcarehub.clinicalattention.domain.model.valueobjects.VeterinarianId;
import com.nexoshift.petcarehub.clinicalattention.support.FakeExternalProfilesService;
import com.nexoshift.petcarehub.clinicalattention.support.InMemoryClinicalConsultationRepository;
import com.nexoshift.petcarehub.shared.domain.exceptions.ForbiddenException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.nexoshift.petcarehub.clinicalattention.support.ClinicalFixtures.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Reglas de lectura de consultas (quién puede ver qué). Escenario:
 * <pre>
 *   Clínica 3 (CLINIC_ID): vet 100 (user 10) y vet 101 (user 11). Paciente: mascota 5.
 *   Clínica 4 (OTHER_CLINIC_ID): vet 102 (user 12). Sin pacientes.
 *   Mascota 5: dueño user 20.  Mascota 6: dueño user 30 (STRANGER), no es paciente de nadie.
 * </pre>
 */
class ClinicalConsultationQueryServiceImplTest {

  private static final long OTHER_PET_ID = 6L;
  private static final long STRANGER_USER_ID = 30L;
  private static final long OTHER_CLINIC_ID = 4L;
  private static final long OTHER_CLINIC_USER_ID = 12L;
  private static final long OTHER_CLINIC_VETERINARIAN_ID = 102L;

  private InMemoryClinicalConsultationRepository repository;
  private FakeExternalProfilesService profiles;
  private ClinicalConsultationQueryServiceImpl service;
  private Long firstId;
  private Long secondId;
  private Long otherPetConsultationId;

  @BeforeEach
  void setUp() {
    repository = new InMemoryClinicalConsultationRepository();
    profiles = new FakeExternalProfilesService()
        .withPet(PET_ID, OWNER_USER_ID)
        .withPet(OTHER_PET_ID, STRANGER_USER_ID)
        .withVeterinarian(USER_ID, VETERINARIAN_ID)
        .withVeterinarian(OTHER_USER_ID, OTHER_VETERINARIAN_ID)
        .withVeterinarian(OTHER_CLINIC_USER_ID, OTHER_CLINIC_VETERINARIAN_ID)
        .withVeterinarianInClinic(VETERINARIAN_ID, CLINIC_ID)
        .withVeterinarianInClinic(OTHER_VETERINARIAN_ID, CLINIC_ID)
        .withVeterinarianInClinic(OTHER_CLINIC_VETERINARIAN_ID, OTHER_CLINIC_ID)
        .withPatient(PET_ID, CLINIC_ID);
    service = new ClinicalConsultationQueryServiceImpl(repository, profiles);
    firstId = save(PET_ID, VETERINARIAN_ID);
    secondId = save(PET_ID, OTHER_VETERINARIAN_ID);
    otherPetConsultationId = save(OTHER_PET_ID, VETERINARIAN_ID);
  }

  private Long save(long petId, long veterinarianId) {
    var command = new CreateClinicalConsultationCommand(
        petId, USER_ID, CLINIC_ID, null, "Check-up");
    return repository.save(new ClinicalConsultation(command, new VeterinarianId(veterinarianId)))
        .getId();
  }

  // ------------------------------------------------ uso interno (fachada)

  @Test
  @DisplayName("Internal query: finds a consultation by id without checking the requester")
  void findsById() {
    var result = service.handle(new GetClinicalConsultationByIdQuery(firstId));

    assertThat(result).isPresent();
    assertThat(result.orElseThrow().getId()).isEqualTo(firstId);
  }

  @Test
  @DisplayName("Internal query: a consultation that does not exist is an empty result")
  void unknownIdIsEmpty() {
    assertThat(service.handle(new GetClinicalConsultationByIdQuery(99L))).isEmpty();
  }

  // ------------------------------------------------ una consulta (API REST)

  @Test
  @DisplayName("A veterinarian of the clinic sees a consultation of a patient")
  void veterinarianOfClinicSeesConsultation() {
    var result = service.handle(new GetAuthorizedClinicalConsultationByIdQuery(firstId, OTHER_USER_ID));

    assertThat(result).isPresent();
  }

  @Test
  @DisplayName("The owner of the pet sees a consultation of their pet")
  void ownerSeesConsultation() {
    var result = service.handle(new GetAuthorizedClinicalConsultationByIdQuery(firstId, OWNER_USER_ID));

    assertThat(result).isPresent();
  }

  @Test
  @DisplayName("A veterinarian of the clinic sees a consultation made there even if the pet is not a patient")
  void veterinarianSeesConsultationOfOwnClinic() {
    var result = service.handle(
        new GetAuthorizedClinicalConsultationByIdQuery(otherPetConsultationId, USER_ID));

    assertThat(result).isPresent();
  }

  @Test
  @DisplayName("A veterinarian of another clinic cannot see the consultation (403)")
  void veterinarianOfOtherClinicCannotSeeConsultation() {
    assertThatThrownBy(() -> service.handle(
        new GetAuthorizedClinicalConsultationByIdQuery(firstId, OTHER_CLINIC_USER_ID)))
        .isInstanceOf(ForbiddenException.class);
  }

  @Test
  @DisplayName("Another pet owner cannot see the consultation (403)")
  void otherOwnerCannotSeeConsultation() {
    assertThatThrownBy(() -> service.handle(
        new GetAuthorizedClinicalConsultationByIdQuery(firstId, STRANGER_USER_ID)))
        .isInstanceOf(ForbiddenException.class);
  }

  @Test
  @DisplayName("When the pet becomes a patient of another clinic, its veterinarians see the history")
  void newClinicSeesHistory() {
    profiles.withPatient(PET_ID, OTHER_CLINIC_ID);

    var result = service.handle(
        new GetAuthorizedClinicalConsultationByIdQuery(firstId, OTHER_CLINIC_USER_ID));

    assertThat(result).isPresent();
  }

  @Test
  @DisplayName("Authorized query: a consultation that does not exist is an empty result (404)")
  void authorizedUnknownIdIsEmpty() {
    assertThat(service.handle(new GetAuthorizedClinicalConsultationByIdQuery(99L, USER_ID)))
        .isEmpty();
  }

  // ------------------------------------------------ historial de la mascota

  @Test
  @DisplayName("A veterinarian of a clinic where the pet is a patient sees its consultations, newest first")
  void veterinarianSeesConsultationsOfPet() {
    var result = service.handle(new GetClinicalConsultationsByPetIdQuery(PET_ID, USER_ID));

    assertThat(result).hasSize(2);
    assertThat(result.get(0).getId()).isEqualTo(secondId);
    assertThat(result.get(1).getId()).isEqualTo(firstId);
  }

  @Test
  @DisplayName("The owner of the pet sees its consultations")
  void ownerSeesConsultationsOfPet() {
    var result = service.handle(new GetClinicalConsultationsByPetIdQuery(PET_ID, OWNER_USER_ID));

    assertThat(result).hasSize(2);
  }

  @Test
  @DisplayName("Another pet owner cannot see them (403)")
  void strangerCannotSeeConsultationsOfPet() {
    assertThatThrownBy(
        () -> service.handle(new GetClinicalConsultationsByPetIdQuery(PET_ID, STRANGER_USER_ID)))
        .isInstanceOf(ForbiddenException.class);
  }

  @Test
  @DisplayName("A veterinarian of a clinic where the pet is not a patient cannot see them (403)")
  void veterinarianOfOtherClinicCannotSeeConsultationsOfPet() {
    assertThatThrownBy(() -> service.handle(
        new GetClinicalConsultationsByPetIdQuery(PET_ID, OTHER_CLINIC_USER_ID)))
        .isInstanceOf(ForbiddenException.class);
  }

  @Test
  @DisplayName("The consultations of a pet that does not exist are a not found error")
  void unknownPet() {
    assertThatThrownBy(
        () -> service.handle(new GetClinicalConsultationsByPetIdQuery(99L, USER_ID)))
        .isInstanceOf(PetNotFoundException.class);
  }

  @Test
  @DisplayName("A pet without consultations gives an empty list")
  void petWithoutConsultations() {
    profiles.withPet(77L, OWNER_USER_ID).withPatient(77L, CLINIC_ID);

    assertThat(service.handle(new GetClinicalConsultationsByPetIdQuery(77L, USER_ID))).isEmpty();
  }

  @Test
  @DisplayName("Lists the consultations of a veterinarian, newest first")
  void listsConsultationsOfVeterinarian() {
    var result = service.handle(new GetClinicalConsultationsByVeterinarianIdQuery(VETERINARIAN_ID));

    assertThat(result).hasSize(2);
    assertThat(result.get(0).getId()).isEqualTo(otherPetConsultationId);
    assertThat(result.get(1).getId()).isEqualTo(firstId);
  }

  @Test
  @DisplayName("The queries validate their ids")
  void queriesValidateIds() {
    assertThatThrownBy(() -> new GetClinicalConsultationByIdQuery(null))
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> new GetAuthorizedClinicalConsultationByIdQuery(1L, null))
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> new GetAuthorizedClinicalConsultationByIdQuery(0L, USER_ID))
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> new GetClinicalConsultationsByPetIdQuery(0L, USER_ID))
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> new GetClinicalConsultationsByPetIdQuery(PET_ID, null))
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> new GetClinicalConsultationsByVeterinarianIdQuery(-1L))
        .isInstanceOf(IllegalArgumentException.class);
  }
}

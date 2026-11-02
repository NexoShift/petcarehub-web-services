package com.nexoshift.petcarehub.clinicalattention.application;

import static com.nexoshift.petcarehub.clinicalattention.support.ClinicalFixtures.CLINIC_ID;
import static com.nexoshift.petcarehub.clinicalattention.support.ClinicalFixtures.OTHER_USER_ID;
import static com.nexoshift.petcarehub.clinicalattention.support.ClinicalFixtures.OTHER_VETERINARIAN_ID;
import static com.nexoshift.petcarehub.clinicalattention.support.ClinicalFixtures.OWNER_USER_ID;
import static com.nexoshift.petcarehub.clinicalattention.support.ClinicalFixtures.PET_ID;
import static com.nexoshift.petcarehub.clinicalattention.support.ClinicalFixtures.USER_ID;
import static com.nexoshift.petcarehub.clinicalattention.support.ClinicalFixtures.VETERINARIAN_ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.nexoshift.petcarehub.clinicalattention.application.internal.queryservices.ClinicalConsultationQueryServiceImpl;
import com.nexoshift.petcarehub.clinicalattention.domain.exceptions.PetNotFoundException;
import com.nexoshift.petcarehub.clinicalattention.domain.model.aggregates.ClinicalConsultation;
import com.nexoshift.petcarehub.clinicalattention.domain.model.commands.CreateClinicalConsultationCommand;
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

class ClinicalConsultationQueryServiceImplTest {

  private static final long OTHER_PET_ID = 6L;
  private static final long STRANGER_USER_ID = 30L;

  private InMemoryClinicalConsultationRepository repository;
  private ClinicalConsultationQueryServiceImpl service;
  private Long firstId;
  private Long secondId;
  private Long otherPetConsultationId;

  @BeforeEach
  void setUp() {
    repository = new InMemoryClinicalConsultationRepository();
    var profiles = new FakeExternalProfilesService()
        .withPet(PET_ID, OWNER_USER_ID)
        .withPet(OTHER_PET_ID, STRANGER_USER_ID)
        .withVeterinarian(USER_ID, VETERINARIAN_ID)
        .withVeterinarian(OTHER_USER_ID, OTHER_VETERINARIAN_ID);
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

  @Test
  @DisplayName("Finds a consultation by id")
  void findsById() {
    var result = service.handle(new GetClinicalConsultationByIdQuery(firstId));

    assertThat(result).isPresent();
    assertThat(result.orElseThrow().getId()).isEqualTo(firstId);
  }

  @Test
  @DisplayName("A consultation that does not exist is an empty result")
  void unknownIdIsEmpty() {
    assertThat(service.handle(new GetClinicalConsultationByIdQuery(99L))).isEmpty();
  }

  @Test
  @DisplayName("A veterinarian sees the consultations of a pet, newest first")
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
  @DisplayName("Someone who is neither a veterinarian nor the owner cannot see them")
  void strangerCannotSeeConsultationsOfPet() {
    assertThatThrownBy(
        () -> service.handle(new GetClinicalConsultationsByPetIdQuery(PET_ID, STRANGER_USER_ID)))
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
    var profiles = new FakeExternalProfilesService()
        .withPet(77L, OWNER_USER_ID).withVeterinarian(USER_ID, VETERINARIAN_ID);
    var emptyService = new ClinicalConsultationQueryServiceImpl(repository, profiles);

    assertThat(emptyService.handle(new GetClinicalConsultationsByPetIdQuery(77L, USER_ID))).isEmpty();
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
    assertThatThrownBy(() -> new GetClinicalConsultationsByPetIdQuery(0L, USER_ID))
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> new GetClinicalConsultationsByPetIdQuery(PET_ID, null))
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> new GetClinicalConsultationsByVeterinarianIdQuery(-1L))
        .isInstanceOf(IllegalArgumentException.class);
  }
}

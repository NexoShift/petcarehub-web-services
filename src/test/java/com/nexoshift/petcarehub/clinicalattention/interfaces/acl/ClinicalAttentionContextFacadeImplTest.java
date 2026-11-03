package com.nexoshift.petcarehub.clinicalattention.interfaces.acl;

import static com.nexoshift.petcarehub.clinicalattention.support.ClinicalFixtures.CONSULTATION_ID;
import static com.nexoshift.petcarehub.clinicalattention.support.ClinicalFixtures.OWNER_USER_ID;
import static com.nexoshift.petcarehub.clinicalattention.support.ClinicalFixtures.PET_ID;
import static com.nexoshift.petcarehub.clinicalattention.support.ClinicalFixtures.USER_ID;
import static com.nexoshift.petcarehub.clinicalattention.support.ClinicalFixtures.VETERINARIAN_ID;
import static com.nexoshift.petcarehub.clinicalattention.support.ClinicalFixtures.createCommand;
import static org.assertj.core.api.Assertions.assertThat;

import com.nexoshift.petcarehub.clinicalattention.application.internal.queryservices.ClinicalConsultationQueryServiceImpl;
import com.nexoshift.petcarehub.clinicalattention.domain.model.aggregates.ClinicalConsultation;
import com.nexoshift.petcarehub.clinicalattention.domain.model.valueobjects.VeterinarianId;
import com.nexoshift.petcarehub.clinicalattention.support.FakeExternalProfilesService;
import com.nexoshift.petcarehub.clinicalattention.support.InMemoryClinicalConsultationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ClinicalAttentionContextFacadeImplTest {

  private InMemoryClinicalConsultationRepository repository;
  private ClinicalAttentionContextFacade facade;

  @BeforeEach
  void setUp() {
    repository = new InMemoryClinicalConsultationRepository();
    var profiles = new FakeExternalProfilesService()
        .withPet(PET_ID, OWNER_USER_ID).withVeterinarian(USER_ID, VETERINARIAN_ID);
    facade = new ClinicalAttentionContextFacadeImpl(
        new ClinicalConsultationQueryServiceImpl(repository, profiles));
  }

  private ClinicalConsultation saveConsultation() {
    return repository.save(new ClinicalConsultation(createCommand(), new VeterinarianId(VETERINARIAN_ID)));
  }

  @Test
  @DisplayName("Tells whether a consultation exists")
  void existsConsultation() {
    saveConsultation();

    assertThat(facade.existsConsultation(CONSULTATION_ID)).isTrue();
    assertThat(facade.existsConsultation(99L)).isFalse();
  }

  @Test
  @DisplayName("Tells whether a consultation is in progress")
  void isConsultationInProgress() {
    var consultation = saveConsultation();
    assertThat(facade.isConsultationInProgress(CONSULTATION_ID)).isFalse();

    consultation.start();
    repository.save(consultation);

    assertThat(facade.isConsultationInProgress(CONSULTATION_ID)).isTrue();
    assertThat(facade.isConsultationInProgress(99L)).isFalse();
  }

  @Test
  @DisplayName("A cancelled consultation is not in progress")
  void cancelledIsNotInProgress() {
    var consultation = saveConsultation();
    consultation.cancel("No show");
    repository.save(consultation);

    assertThat(facade.isConsultationInProgress(CONSULTATION_ID)).isFalse();
  }

  @Test
  @DisplayName("Gives the pet and the veterinarian of the consultation as simple ids")
  void fetchesIds() {
    saveConsultation();

    assertThat(facade.fetchPetIdByConsultationId(CONSULTATION_ID)).isEqualTo(PET_ID);
    assertThat(facade.fetchVeterinarianIdByConsultationId(CONSULTATION_ID))
        .isEqualTo(VETERINARIAN_ID);
  }

  @Test
  @DisplayName("Gives 0 when the consultation does not exist")
  void unknownConsultationGivesZero() {
    assertThat(facade.fetchPetIdByConsultationId(99L)).isEqualTo(0L);
    assertThat(facade.fetchVeterinarianIdByConsultationId(99L)).isEqualTo(0L);
  }

  @Test
  @DisplayName("Missing or invalid ids are answered without failing")
  void invalidIds() {
    assertThat(facade.existsConsultation(null)).isFalse();
    assertThat(facade.existsConsultation(0L)).isFalse();
    assertThat(facade.isConsultationInProgress(null)).isFalse();
    assertThat(facade.fetchPetIdByConsultationId(null)).isEqualTo(0L);
    assertThat(facade.fetchVeterinarianIdByConsultationId(-5L)).isEqualTo(0L);
  }
}

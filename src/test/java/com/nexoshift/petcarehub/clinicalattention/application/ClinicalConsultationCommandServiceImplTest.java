package com.nexoshift.petcarehub.clinicalattention.application;

import static com.nexoshift.petcarehub.clinicalattention.support.ClinicalFixtures.CLINIC_ID;
import static com.nexoshift.petcarehub.clinicalattention.support.ClinicalFixtures.OTHER_USER_ID;
import static com.nexoshift.petcarehub.clinicalattention.support.ClinicalFixtures.OTHER_VETERINARIAN_ID;
import static com.nexoshift.petcarehub.clinicalattention.support.ClinicalFixtures.OWNER_USER_ID;
import static com.nexoshift.petcarehub.clinicalattention.support.ClinicalFixtures.PET_ID;
import static com.nexoshift.petcarehub.clinicalattention.support.ClinicalFixtures.ROOM_ID;
import static com.nexoshift.petcarehub.clinicalattention.support.ClinicalFixtures.USER_ID;
import static com.nexoshift.petcarehub.clinicalattention.support.ClinicalFixtures.VETERINARIAN_ID;
import static com.nexoshift.petcarehub.clinicalattention.support.ClinicalFixtures.anamnesisCommand;
import static com.nexoshift.petcarehub.clinicalattention.support.ClinicalFixtures.assessmentCommand;
import static com.nexoshift.petcarehub.clinicalattention.support.ClinicalFixtures.createCommand;
import static com.nexoshift.petcarehub.clinicalattention.support.ClinicalFixtures.findingCommand;
import static com.nexoshift.petcarehub.clinicalattention.support.ClinicalFixtures.primaryDiagnosisCommand;
import static com.nexoshift.petcarehub.clinicalattention.support.ClinicalFixtures.reevaluationCommand;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.nexoshift.petcarehub.clinicalattention.application.internal.commandservices.ClinicalConsultationCommandServiceImpl;
import com.nexoshift.petcarehub.clinicalattention.domain.exceptions.ClinicalConsultationNotFoundException;
import com.nexoshift.petcarehub.clinicalattention.domain.exceptions.IncompleteConsultationException;
import com.nexoshift.petcarehub.clinicalattention.domain.exceptions.InvalidConsultationStateException;
import com.nexoshift.petcarehub.clinicalattention.domain.exceptions.PetNotFoundException;
import com.nexoshift.petcarehub.clinicalattention.domain.exceptions.VeterinarianNotAuthorizedException;
import com.nexoshift.petcarehub.clinicalattention.domain.model.commands.CancelClinicalConsultationCommand;
import com.nexoshift.petcarehub.clinicalattention.domain.model.commands.CompleteClinicalConsultationCommand;
import com.nexoshift.petcarehub.clinicalattention.domain.model.commands.CreateClinicalConsultationCommand;
import com.nexoshift.petcarehub.clinicalattention.domain.model.commands.RegisterAnamnesisCommand;
import com.nexoshift.petcarehub.clinicalattention.domain.model.commands.StartClinicalConsultationCommand;
import com.nexoshift.petcarehub.clinicalattention.domain.model.events.ClinicalConsultationCancelledEvent;
import com.nexoshift.petcarehub.clinicalattention.domain.model.events.ClinicalConsultationCompletedEvent;
import com.nexoshift.petcarehub.clinicalattention.domain.model.events.ClinicalConsultationCreatedEvent;
import com.nexoshift.petcarehub.clinicalattention.domain.model.valueobjects.ConsultationStatus;
import com.nexoshift.petcarehub.clinicalattention.domain.model.valueobjects.DiagnosisStatus;
import com.nexoshift.petcarehub.clinicalattention.support.FakeExternalProfilesService;
import com.nexoshift.petcarehub.clinicalattention.support.InMemoryClinicalConsultationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ClinicalConsultationCommandServiceImplTest {

  private InMemoryClinicalConsultationRepository repository;
  private FakeExternalProfilesService profiles;
  private ClinicalConsultationCommandServiceImpl service;

  @BeforeEach
  void setUp() {
    repository = new InMemoryClinicalConsultationRepository();
    profiles = new FakeExternalProfilesService()
        .withPet(PET_ID, OWNER_USER_ID)
        .withVeterinarian(USER_ID, VETERINARIAN_ID)
        .withVeterinarian(OTHER_USER_ID, OTHER_VETERINARIAN_ID)
        .withVeterinarianInClinic(VETERINARIAN_ID, CLINIC_ID)
        .withVeterinarianInClinic(OTHER_VETERINARIAN_ID, CLINIC_ID)
        .withConsultingRoom(CLINIC_ID, ROOM_ID);
    service = new ClinicalConsultationCommandServiceImpl(repository, profiles);
  }

  private Long createConsultation() {
    return service.handle(createCommand()).orElseThrow().getId();
  }

  private Long startedConsultation() {
    var id = createConsultation();
    service.handle(new StartClinicalConsultationCommand(id, USER_ID));
    return id;
  }

  // ------------------------------------------------------------------ create

  @Test
  @DisplayName("Creating a consultation saves it as CREATED for the veterinarian of the user")
  void createsConsultation() {
    var consultation = service.handle(createCommand()).orElseThrow();

    assertThat(consultation.getId()).isNotNull();
    assertThat(consultation.getStatus()).isEqualTo(ConsultationStatus.CREATED);
    assertThat(consultation.getPetId().petId()).isEqualTo(PET_ID);
    assertThat(consultation.getVeterinarianId().veterinarianId()).isEqualTo(VETERINARIAN_ID);
    assertThat(consultation.getClinicId().clinicId()).isEqualTo(CLINIC_ID);
    assertThat(repository.size()).isEqualTo(1);
  }

  @Test
  @DisplayName("The created event is published after the first save, so it has the id")
  void publishesCreatedEventWithId() {
    var consultation = service.handle(createCommand()).orElseThrow();

    var events = repository.publishedEvents(ClinicalConsultationCreatedEvent.class);
    assertThat(events).hasSize(1);
    assertThat(events.get(0).consultationId()).isEqualTo(consultation.getId());
    assertThat(events.get(0).petId()).isEqualTo(PET_ID);
    assertThat(events.get(0).veterinarianId()).isEqualTo(VETERINARIAN_ID);
    assertThat(repository.saveCount()).isEqualTo(2);
  }

  @Test
  @DisplayName("A consultation for a pet that does not exist is rejected")
  void rejectsUnknownPet() {
    var command = new CreateClinicalConsultationCommand(99L, USER_ID, CLINIC_ID, null, "Check-up");

    assertThatThrownBy(() -> service.handle(command)).isInstanceOf(PetNotFoundException.class);
    assertThat(repository.size()).isEqualTo(0);
  }

  @Test
  @DisplayName("A user that is not a veterinarian cannot create a consultation")
  void rejectsUserWithoutVeterinarianProfile() {
    var command = new CreateClinicalConsultationCommand(
        PET_ID, OWNER_USER_ID, CLINIC_ID, null, "Check-up");

    assertThatThrownBy(() -> service.handle(command))
        .isInstanceOf(VeterinarianNotAuthorizedException.class);
    assertThat(repository.size()).isEqualTo(0);
  }

  @Test
  @DisplayName("A veterinarian that is not active in the clinic cannot create a consultation")
  void rejectsVeterinarianOutsideClinic() {
    var command = new CreateClinicalConsultationCommand(PET_ID, USER_ID, 99L, null, "Check-up");

    assertThatThrownBy(() -> service.handle(command))
        .isInstanceOf(VeterinarianNotAuthorizedException.class);
    assertThat(repository.size()).isEqualTo(0);
  }

  @Test
  @DisplayName("The consulting room must be active in the clinic when it is given")
  void validatesConsultingRoom() {
    var unknownRoom = new CreateClinicalConsultationCommand(
        PET_ID, USER_ID, CLINIC_ID, 99L, "Check-up");
    var noRoom = new CreateClinicalConsultationCommand(
        PET_ID, USER_ID, CLINIC_ID, null, "Check-up");

    assertThatThrownBy(() -> service.handle(unknownRoom))
        .isInstanceOf(IllegalArgumentException.class);
    assertThat(service.handle(noRoom)).isPresent();
  }

  // ----------------------------------------------------------- full lifecycle

  @Test
  @DisplayName("Complete lifecycle: start, anamnesis, assessments, finding, diagnosis, reevaluation and completion")
  void fullLifecycle() {
    var id = createConsultation();

    var started = service.handle(new StartClinicalConsultationCommand(id, USER_ID)).orElseThrow();
    assertThat(started.getStatus()).isEqualTo(ConsultationStatus.IN_PROGRESS);

    service.handle(anamnesisCommand(id));
    service.handle(assessmentCommand(id));
    service.handle(assessmentCommand(id));
    service.handle(findingCommand(id));
    var withDiagnosis = service.handle(primaryDiagnosisCommand(id)).orElseThrow();
    var diagnosisId = withDiagnosis.getDiagnoses().get(0).getId();
    assertThat(diagnosisId).isNotNull();

    var reevaluated = service.handle(reevaluationCommand(id, diagnosisId)).orElseThrow();
    assertThat(reevaluated.getDiagnoses()).hasSize(2);
    assertThat(reevaluated.getDiagnoses().get(0).getStatus()).isEqualTo(DiagnosisStatus.SUPERSEDED);
    assertThat(reevaluated.getDiagnoses().get(1).getPreviousDiagnosisId()).isEqualTo(diagnosisId);

    var completed = service.handle(new CompleteClinicalConsultationCommand(id, USER_ID))
        .orElseThrow();

    assertThat(completed.getStatus()).isEqualTo(ConsultationStatus.COMPLETED);
    assertThat(completed.getAnamnesis()).isNotNull();
    assertThat(completed.getPhysiologicalAssessments()).hasSize(2);
    assertThat(completed.getClinicalFindings()).hasSize(1);
    var completedEvents = repository.publishedEvents(ClinicalConsultationCompletedEvent.class);
    assertThat(completedEvents).hasSize(1);
    assertThat(completedEvents.get(0).consultationId()).isEqualTo(id);
    assertThat(completedEvents.get(0).primaryDiagnosis()).isEqualTo("Pancreatitis");
  }

  @Test
  @DisplayName("Completing an incomplete consultation reports what is missing and publishes nothing")
  void incompleteCompletion() {
    var id = startedConsultation();
    service.handle(anamnesisCommand(id));

    assertThatThrownBy(() -> service.handle(new CompleteClinicalConsultationCommand(id, USER_ID)))
        .isInstanceOfSatisfying(IncompleteConsultationException.class, exception ->
            assertThat(exception.getPendingItems())
                .containsExactly("physiologicalAssessment", "primaryDiagnosis"));

    assertThat(repository.findById(id).orElseThrow().getStatus())
        .isEqualTo(ConsultationStatus.IN_PROGRESS);
    assertThat(repository.publishedEvents(ClinicalConsultationCompletedEvent.class)).isEmpty();
  }

  @Test
  @DisplayName("Cancelling saves the reason and publishes the cancelled event")
  void cancelsConsultation() {
    var id = createConsultation();

    var cancelled = service.handle(new CancelClinicalConsultationCommand(id, USER_ID, "No show"))
        .orElseThrow();

    assertThat(cancelled.getStatus()).isEqualTo(ConsultationStatus.CANCELLED);
    assertThat(cancelled.getCancellation().reason()).isEqualTo("No show");
    var events = repository.publishedEvents(ClinicalConsultationCancelledEvent.class);
    assertThat(events).hasSize(1);
    assertThat(events.get(0).consultationId()).isEqualTo(id);
    assertThat(events.get(0).reason()).isEqualTo("No show");
  }

  @Test
  @DisplayName("The anamnesis cannot be registered twice through the service")
  void anamnesisTwice() {
    var id = startedConsultation();
    service.handle(anamnesisCommand(id));

    assertThatThrownBy(() -> service.handle(anamnesisCommand(id)))
        .isInstanceOf(InvalidConsultationStateException.class);
  }

  @Test
  @DisplayName("A diagnosis that is not in the consultation cannot be reevaluated")
  void reevaluatesUnknownDiagnosis() {
    var id = startedConsultation();
    service.handle(primaryDiagnosisCommand(id));

    assertThatThrownBy(() -> service.handle(reevaluationCommand(id, 999L)))
        .isInstanceOf(IllegalArgumentException.class);
  }

  // ------------------------------------------------------ authorization/errors

  @Test
  @DisplayName("Any operation on a consultation that does not exist is a not found error")
  void unknownConsultation() {
    assertThatThrownBy(() -> service.handle(new StartClinicalConsultationCommand(99L, USER_ID)))
        .isInstanceOf(ClinicalConsultationNotFoundException.class);
    assertThatThrownBy(() -> service.handle(anamnesisCommand(99L)))
        .isInstanceOf(ClinicalConsultationNotFoundException.class);
    assertThatThrownBy(() -> service.handle(assessmentCommand(99L)))
        .isInstanceOf(ClinicalConsultationNotFoundException.class);
    assertThatThrownBy(() -> service.handle(findingCommand(99L)))
        .isInstanceOf(ClinicalConsultationNotFoundException.class);
    assertThatThrownBy(() -> service.handle(primaryDiagnosisCommand(99L)))
        .isInstanceOf(ClinicalConsultationNotFoundException.class);
    assertThatThrownBy(() -> service.handle(reevaluationCommand(99L, 1L)))
        .isInstanceOf(ClinicalConsultationNotFoundException.class);
    assertThatThrownBy(() -> service.handle(new CompleteClinicalConsultationCommand(99L, USER_ID)))
        .isInstanceOf(ClinicalConsultationNotFoundException.class);
    assertThatThrownBy(
        () -> service.handle(new CancelClinicalConsultationCommand(99L, USER_ID, "No show")))
        .isInstanceOf(ClinicalConsultationNotFoundException.class);
  }

  @Test
  @DisplayName("Only the veterinarian in charge can change the consultation")
  void otherVeterinarianCannotChange() {
    var id = startedConsultation();
    var saves = repository.saveCount();

    assertThatThrownBy(() -> service.handle(new StartClinicalConsultationCommand(id, OTHER_USER_ID)))
        .isInstanceOf(VeterinarianNotAuthorizedException.class);
    assertThatThrownBy(() -> service.handle(anamnesisBy(id, OTHER_USER_ID)))
        .isInstanceOf(VeterinarianNotAuthorizedException.class);
    assertThatThrownBy(() -> service.handle(
        new CompleteClinicalConsultationCommand(id, OTHER_USER_ID)))
        .isInstanceOf(VeterinarianNotAuthorizedException.class);
    assertThatThrownBy(() -> service.handle(
        new CancelClinicalConsultationCommand(id, OTHER_USER_ID, "No show")))
        .isInstanceOf(VeterinarianNotAuthorizedException.class);

    assertThat(repository.saveCount()).isEqualTo(saves);
    assertThat(repository.findById(id).orElseThrow().getStatus())
        .isEqualTo(ConsultationStatus.IN_PROGRESS);
    assertThat(repository.findById(id).orElseThrow().getAnamnesis()).isNull();
  }

  @Test
  @DisplayName("A user that is not a veterinarian cannot change the consultation")
  void ownerCannotChange() {
    var id = createConsultation();

    assertThatThrownBy(() -> service.handle(new StartClinicalConsultationCommand(id, OWNER_USER_ID)))
        .isInstanceOf(VeterinarianNotAuthorizedException.class);
  }

  private static RegisterAnamnesisCommand anamnesisBy(Long consultationId, Long requesterUserId) {
    var base = anamnesisCommand(consultationId);
    return new RegisterAnamnesisCommand(consultationId, requesterUserId, base.chiefComplaint(),
        base.evolutionTime(), base.symptoms(), base.diet(), base.behavior(),
        base.previousTreatments(), base.ownerObservations());
  }
}

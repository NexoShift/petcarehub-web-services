package com.nexoshift.petcarehub.clinicalattention.domain.model.aggregates;

import com.nexoshift.petcarehub.clinicalattention.domain.exceptions.IncompleteConsultationException;
import com.nexoshift.petcarehub.clinicalattention.domain.exceptions.InvalidConsultationStateException;
import com.nexoshift.petcarehub.clinicalattention.domain.model.commands.AddClinicalFindingCommand;
import com.nexoshift.petcarehub.clinicalattention.domain.model.commands.CreateClinicalConsultationCommand;
import com.nexoshift.petcarehub.clinicalattention.domain.model.commands.ReevaluateDiagnosisCommand;
import com.nexoshift.petcarehub.clinicalattention.domain.model.commands.RegisterAnamnesisCommand;
import com.nexoshift.petcarehub.clinicalattention.domain.model.commands.RegisterDiagnosisCommand;
import com.nexoshift.petcarehub.clinicalattention.domain.model.commands.RegisterPhysiologicalAssessmentCommand;
import com.nexoshift.petcarehub.clinicalattention.domain.model.entities.Anamnesis;
import com.nexoshift.petcarehub.clinicalattention.domain.model.entities.ClinicalFinding;
import com.nexoshift.petcarehub.clinicalattention.domain.model.entities.Diagnosis;
import com.nexoshift.petcarehub.clinicalattention.domain.model.entities.PhysiologicalAssessment;
import com.nexoshift.petcarehub.clinicalattention.domain.model.events.ClinicalConsultationCancelledEvent;
import com.nexoshift.petcarehub.clinicalattention.domain.model.events.ClinicalConsultationCompletedEvent;
import com.nexoshift.petcarehub.clinicalattention.domain.model.events.ClinicalConsultationCreatedEvent;
import com.nexoshift.petcarehub.clinicalattention.domain.model.valueobjects.BodyConditionScore;
import com.nexoshift.petcarehub.clinicalattention.domain.model.valueobjects.CancellationDetail;
import com.nexoshift.petcarehub.clinicalattention.domain.model.valueobjects.ClinicId;
import com.nexoshift.petcarehub.clinicalattention.domain.model.valueobjects.ClinicalFieldLimits;
import com.nexoshift.petcarehub.clinicalattention.domain.model.valueobjects.ConsultationStatus;
import com.nexoshift.petcarehub.clinicalattention.domain.model.valueobjects.ConsultingRoomId;
import com.nexoshift.petcarehub.clinicalattention.domain.model.valueobjects.PetId;
import com.nexoshift.petcarehub.clinicalattention.domain.model.valueobjects.VeterinarianId;
import com.nexoshift.petcarehub.clinicalattention.domain.model.valueobjects.VitalSigns;
import com.nexoshift.petcarehub.shared.domain.model.aggregates.AuditableAbstractAggregateRoot;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.OrderBy;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

/**
 * Aggregate Root of Clinical Attention (ConsultaClinica). Controls one veterinary attention:
 * CREATED, then IN_PROGRESS, then COMPLETED or CANCELLED.
 *
 * <p>Pet, veterinarian, clinic and consulting room belong to Profiles &amp; Organization, so they
 * are referenced by id only. Clinical records are never deleted or overwritten (CON-01): the
 * anamnesis is registered once, every physiological assessment, finding and diagnosis is a new
 * record, and a reevaluation adds a diagnosis and marks the previous one as superseded.
 */
@Entity
public class ClinicalConsultation extends AuditableAbstractAggregateRoot<ClinicalConsultation> {

  @Embedded
  @AttributeOverride(name = "petId", column = @Column(name = "pet_id", nullable = false))
  private PetId petId;

  @Embedded
  @AttributeOverride(name = "veterinarianId",
      column = @Column(name = "veterinarian_id", nullable = false))
  private VeterinarianId veterinarianId;

  @Embedded
  @AttributeOverride(name = "clinicId", column = @Column(name = "clinic_id", nullable = false))
  private ClinicId clinicId;

  @Embedded
  @AttributeOverride(name = "consultingRoomId", column = @Column(name = "consulting_room_id"))
  private ConsultingRoomId consultingRoomId;

  @Column(nullable = false, length = ClinicalFieldLimits.REASON)
  private String reason;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private ConsultationStatus status;

  private Instant startedAt;

  private Instant completedAt;

  @Embedded
  private CancellationDetail cancellation;

  @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
  @JoinColumn(name = "anamnesis_id")
  private Anamnesis anamnesis;

  @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
  @JoinColumn(name = "clinical_consultation_id", nullable = false, updatable = false)
  @OrderBy("id ASC")
  @Fetch(FetchMode.SUBSELECT)
  private List<PhysiologicalAssessment> physiologicalAssessments = new ArrayList<>();

  @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
  @JoinColumn(name = "clinical_consultation_id", nullable = false, updatable = false)
  @OrderBy("id ASC")
  @Fetch(FetchMode.SUBSELECT)
  private List<ClinicalFinding> clinicalFindings = new ArrayList<>();

  @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
  @JoinColumn(name = "clinical_consultation_id", nullable = false, updatable = false)
  @OrderBy("id ASC")
  @Fetch(FetchMode.SUBSELECT)
  private List<Diagnosis> diagnoses = new ArrayList<>();

  protected ClinicalConsultation() {
  }

  /** Creates a consultation in CREATED status for the veterinarian in charge. */
  public ClinicalConsultation(
      CreateClinicalConsultationCommand command, VeterinarianId veterinarianId) {
    if (veterinarianId == null) {
      throw new IllegalArgumentException("Veterinarian id is required");
    }
    this.petId = new PetId(command.petId());
    this.veterinarianId = veterinarianId;
    this.clinicId = new ClinicId(command.clinicId());
    this.consultingRoomId = command.consultingRoomId() == null
        ? null
        : new ConsultingRoomId(command.consultingRoomId());
    this.reason = command.reason();
    this.status = ConsultationStatus.CREATED;
  }

  /** Registers the creation event. Call it after the first save, when the id exists. */
  public void notifyCreated() {
    registerEvent(new ClinicalConsultationCreatedEvent(
        getId(), petId.petId(), veterinarianId.veterinarianId(), Instant.now()));
  }

  /** CREATED to IN_PROGRESS: the veterinarian starts the attention. */
  public void start() {
    requireStatus(ConsultationStatus.CREATED, "be started");
    this.status = ConsultationStatus.IN_PROGRESS;
    this.startedAt = Instant.now();
  }

  /** Registers the anamnesis. There is only one per consultation. */
  public Anamnesis registerAnamnesis(RegisterAnamnesisCommand command) {
    requireStatus(ConsultationStatus.IN_PROGRESS, "register the anamnesis");
    if (anamnesis != null) {
      throw new InvalidConsultationStateException(
          "The anamnesis of this clinical consultation was already registered");
    }
    anamnesis = new Anamnesis(command.chiefComplaint(), command.evolutionTime(),
        command.symptoms(), command.diet(), command.behavior(), command.previousTreatments(),
        command.ownerObservations());
    return anamnesis;
  }

  /** Registers a new measurement. Earlier measurements are kept. */
  public PhysiologicalAssessment registerPhysiologicalAssessment(
      RegisterPhysiologicalAssessmentCommand command) {
    requireStatus(ConsultationStatus.IN_PROGRESS, "register a physiological assessment");
    var vitalSigns = new VitalSigns(command.weightKg(), command.temperatureCelsius(),
        command.heartRateBpm(), command.respiratoryRateBpm());
    var bodyConditionScore = command.bodyConditionScore() == null
        ? null
        : new BodyConditionScore(command.bodyConditionScore());
    var assessment = new PhysiologicalAssessment(vitalSigns, bodyConditionScore,
        command.hydrationStatus(), command.mucousMembranes(),
        command.capillaryRefillTimeSeconds(), command.observations());
    physiologicalAssessments.add(assessment);
    return assessment;
  }

  /** Adds an observation found during the examination. */
  public ClinicalFinding addClinicalFinding(AddClinicalFindingCommand command) {
    requireStatus(ConsultationStatus.IN_PROGRESS, "add a clinical finding");
    var finding = new ClinicalFinding(
        command.bodySystem(), command.description(), command.severity());
    clinicalFindings.add(finding);
    return finding;
  }

  /**
   * Registers a diagnosis made by the veterinarian in charge. There can be only one current
   * primary diagnosis: to change it, reevaluate it.
   */
  public Diagnosis registerDiagnosis(RegisterDiagnosisCommand command) {
    requireStatus(ConsultationStatus.IN_PROGRESS, "register a diagnosis");
    if (command.primary() && currentPrimaryDiagnosis().isPresent()) {
      throw new InvalidConsultationStateException(
          "The clinical consultation already has a current primary diagnosis; reevaluate it");
    }
    var diagnosis = new Diagnosis(command.description(), command.type(), command.primary(),
        command.differentialDiagnoses(), command.rationale(), veterinarianId, null);
    diagnoses.add(diagnosis);
    return diagnosis;
  }

  /**
   * Reevaluates a diagnosis: a new diagnosis is added, with the same importance as the previous
   * one, and the previous one stays as it was, marked as SUPERSEDED (CON-01).
   */
  public Diagnosis reevaluateDiagnosis(ReevaluateDiagnosisCommand command) {
    requireStatus(ConsultationStatus.IN_PROGRESS, "reevaluate a diagnosis");
    var previous = diagnoses.stream()
        .filter(diagnosis -> command.previousDiagnosisId().equals(diagnosis.getId()))
        .findFirst()
        .orElseThrow(() -> new IllegalArgumentException(
            "The diagnosis to reevaluate does not belong to this clinical consultation"));
    if (!previous.isCurrent()) {
      throw new InvalidConsultationStateException("The diagnosis was already reevaluated");
    }
    var reevaluation = new Diagnosis(command.description(), command.type(), previous.isPrimary(),
        command.differentialDiagnoses(), command.rationale(), veterinarianId, previous.getId());
    previous.markAsSuperseded();
    diagnoses.add(reevaluation);
    return reevaluation;
  }

  /**
   * IN_PROGRESS to COMPLETED. It needs the anamnesis, at least one physiological assessment and a
   * current primary diagnosis. If something is missing the consultation is left untouched and the
   * pending items are reported.
   */
  public void complete() {
    requireStatus(ConsultationStatus.IN_PROGRESS, "be completed");
    var pending = pendingItems();
    if (!pending.isEmpty()) {
      throw new IncompleteConsultationException(pending);
    }
    this.status = ConsultationStatus.COMPLETED;
    this.completedAt = Instant.now();
    var primaryDiagnosis = currentPrimaryDiagnosis().map(Diagnosis::getDescription).orElse(null);
    registerEvent(new ClinicalConsultationCompletedEvent(getId(), petId.petId(),
        veterinarianId.veterinarianId(), primaryDiagnosis, completedAt));
  }

  /** CREATED or IN_PROGRESS to CANCELLED. The reason is required. */
  public void cancel(String reason) {
    if (!status.allowsCancellation()) {
      throw new InvalidConsultationStateException(
          "A clinical consultation that is " + status + " cannot be cancelled");
    }
    var detail = new CancellationDetail(reason, Instant.now());
    this.cancellation = detail;
    this.status = ConsultationStatus.CANCELLED;
    registerEvent(new ClinicalConsultationCancelledEvent(
        getId(), detail.reason(), detail.cancelledAt()));
  }

  public boolean isInProgress() {
    return status == ConsultationStatus.IN_PROGRESS;
  }

  public boolean isAttendedBy(VeterinarianId veterinarianId) {
    return this.veterinarianId.equals(veterinarianId);
  }

  /**
   * Data still required to complete the consultation: anamnesis, physiologicalAssessment and
   * primaryDiagnosis. Empty when the consultation can be completed.
   */
  public List<String> pendingItems() {
    var pending = new ArrayList<String>();
    if (anamnesis == null) {
      pending.add("anamnesis");
    }
    if (physiologicalAssessments.isEmpty()) {
      pending.add("physiologicalAssessment");
    }
    if (currentPrimaryDiagnosis().isEmpty()) {
      pending.add("primaryDiagnosis");
    }
    return pending;
  }

  private Optional<Diagnosis> currentPrimaryDiagnosis() {
    return diagnoses.stream()
        .filter(diagnosis -> diagnosis.isPrimary() && diagnosis.isCurrent())
        .findFirst();
  }

  private void requireStatus(ConsultationStatus expected, String action) {
    if (status != expected) {
      throw new InvalidConsultationStateException("A clinical consultation must be " + expected
          + " to " + action + " (current status: " + status + ")");
    }
  }

  public PetId getPetId() {
    return petId;
  }

  public VeterinarianId getVeterinarianId() {
    return veterinarianId;
  }

  public ClinicId getClinicId() {
    return clinicId;
  }

  public ConsultingRoomId getConsultingRoomId() {
    return consultingRoomId;
  }

  public String getReason() {
    return reason;
  }

  public ConsultationStatus getStatus() {
    return status;
  }

  public Instant getStartedAt() {
    return startedAt;
  }

  public Instant getCompletedAt() {
    return completedAt;
  }

  public CancellationDetail getCancellation() {
    return cancellation;
  }

  public Anamnesis getAnamnesis() {
    return anamnesis;
  }

  public List<PhysiologicalAssessment> getPhysiologicalAssessments() {
    return List.copyOf(physiologicalAssessments);
  }

  public List<ClinicalFinding> getClinicalFindings() {
    return List.copyOf(clinicalFindings);
  }

  public List<Diagnosis> getDiagnoses() {
    return List.copyOf(diagnoses);
  }
}

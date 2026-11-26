package com.nexoshift.petcarehub.medicalhistory.domain.model.aggregates;

import com.nexoshift.petcarehub.medicalhistory.domain.model.commands.RecordAllergyCommand;
import com.nexoshift.petcarehub.medicalhistory.domain.model.commands.RecordConsultationCompletedCommand;
import com.nexoshift.petcarehub.medicalhistory.domain.model.commands.RecordExamResultValidatedCommand;
import com.nexoshift.petcarehub.medicalhistory.domain.model.commands.RecordVaccinationCommand;
import com.nexoshift.petcarehub.medicalhistory.domain.model.entities.Allergy;
import com.nexoshift.petcarehub.medicalhistory.domain.model.entities.MedicalHistoryEntry;
import com.nexoshift.petcarehub.medicalhistory.domain.model.entities.VaccinationRecord;
import com.nexoshift.petcarehub.medicalhistory.domain.model.valueobjects.HistoryEntryType;
import com.nexoshift.petcarehub.medicalhistory.domain.model.valueobjects.PetId;
import com.nexoshift.petcarehub.medicalhistory.domain.model.valueobjects.ReporterType;
import com.nexoshift.petcarehub.shared.domain.model.aggregates.AuditableAbstractAggregateRoot;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Aggregate Root of Medical History (HistoriaClinica): the longitudinal record of one pet,
 * centred on the pet and not on a clinic. It is append-only: allergies, vaccinations and entries
 * are added and never changed or deleted. Entries only keep references to the consultations and
 * exams of the other contexts.
 */
@Entity
public class MedicalHistory extends AuditableAbstractAggregateRoot<MedicalHistory> {

  @Embedded
  @AttributeOverride(name = "petId",
      column = @Column(name = "pet_id", nullable = false, unique = true))
  private PetId petId;

  @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
  @JoinColumn(name = "medical_history_id", nullable = false, updatable = false)
  @OrderBy("id ASC")
  @Fetch(FetchMode.SUBSELECT)
  private List<Allergy> allergies = new ArrayList<>();

  @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
  @JoinColumn(name = "medical_history_id", nullable = false, updatable = false)
  @OrderBy("id ASC")
  @Fetch(FetchMode.SUBSELECT)
  private List<VaccinationRecord> vaccinations = new ArrayList<>();

  @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
  @JoinColumn(name = "medical_history_id", nullable = false, updatable = false)
  @OrderBy("id ASC")
  @Fetch(FetchMode.SUBSELECT)
  private List<MedicalHistoryEntry> entries = new ArrayList<>();

  protected MedicalHistory() {
  }

  public MedicalHistory(Long petId) {
    this.petId = new PetId(petId);
  }

  public Allergy recordAllergy(RecordAllergyCommand command, ReporterType reportedBy) {
    var allergy = new Allergy(command.allergen(), command.reaction(), command.severity(),
        command.notes(), reportedBy, command.requesterUserId());
    allergies.add(allergy);
    return allergy;
  }

  public VaccinationRecord recordVaccination(RecordVaccinationCommand command,
      Long veterinarianId, Long clinicId) {
    var vaccination = new VaccinationRecord(command.vaccineName(), command.doseNumber(),
        command.appliedOn(), command.nextDoseOn(), command.batchNumber(), command.notes(),
        veterinarianId, clinicId);
    vaccinations.add(vaccination);
    return vaccination;
  }

  /** Adds the completed consultation once; a repeated event is ignored. */
  public void recordConsultationCompleted(RecordConsultationCompletedCommand command) {
    if (hasEntry(HistoryEntryType.CONSULTATION_COMPLETED, command.consultationId())) {
      return;
    }
    entries.add(new MedicalHistoryEntry(HistoryEntryType.CONSULTATION_COMPLETED,
        command.consultationId(), command.consultationId(), command.clinicId(),
        command.veterinarianId(), null, command.primaryDiagnosis(), command.completedAt()));
  }

  /** Adds the validated exam result once; a repeated event is ignored. */
  public void recordExamResultValidated(RecordExamResultValidatedCommand command) {
    if (hasEntry(HistoryEntryType.EXAM_RESULT_VALIDATED, command.examId())) {
      return;
    }
    entries.add(new MedicalHistoryEntry(HistoryEntryType.EXAM_RESULT_VALIDATED,
        command.examId(), command.consultationId(), command.clinicId(), null,
        command.examType(), command.resultSummary(), command.validatedAt()));
  }

  private boolean hasEntry(HistoryEntryType type, Long referenceId) {
    return entries.stream().anyMatch(entry -> entry.refersTo(type, referenceId));
  }

  public Long getPetIdValue() {
    return petId.petId();
  }

  public List<Allergy> getAllergies() {
    return List.copyOf(allergies);
  }

  /** Most recent application first. */
  public List<VaccinationRecord> getVaccinations() {
    return vaccinations.stream()
        .sorted(Comparator.comparing(VaccinationRecord::getAppliedOn).reversed())
        .toList();
  }

  /** Timeline, most recent first. */
  public List<MedicalHistoryEntry> getEntries() {
    return entries.stream()
        .sorted(Comparator.comparing(MedicalHistoryEntry::getOccurredAt).reversed())
        .toList();
  }
}

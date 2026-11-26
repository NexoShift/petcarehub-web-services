package com.nexoshift.petcarehub.aiassistance.domain.model.aggregates;

import com.nexoshift.petcarehub.aiassistance.domain.exceptions.InvalidAiAnalysisStateException;
import com.nexoshift.petcarehub.aiassistance.domain.exceptions.SoapDraftNotFoundException;
import com.nexoshift.petcarehub.aiassistance.domain.model.commands.RequestClinicalAssistanceCommand;
import com.nexoshift.petcarehub.aiassistance.domain.model.entities.SoapDraft;
import com.nexoshift.petcarehub.aiassistance.domain.model.events.ClinicalAssistanceGeneratedEvent;
import com.nexoshift.petcarehub.aiassistance.domain.model.valueobjects.AiAnalysisStatus;
import com.nexoshift.petcarehub.aiassistance.domain.model.valueobjects.ClinicalConsultationId;
import com.nexoshift.petcarehub.aiassistance.domain.model.valueobjects.DocumentReference;
import com.nexoshift.petcarehub.aiassistance.domain.model.valueobjects.DraftDecision;
import com.nexoshift.petcarehub.aiassistance.domain.model.valueobjects.PetId;
import com.nexoshift.petcarehub.aiassistance.domain.model.valueobjects.SelectedBackground;
import com.nexoshift.petcarehub.aiassistance.domain.model.valueobjects.VeterinarianId;
import com.nexoshift.petcarehub.shared.domain.exceptions.ForbiddenException;
import com.nexoshift.petcarehub.shared.domain.model.aggregates.AuditableAbstractAggregateRoot;
import jakarta.persistence.CascadeType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

/**
 * A request of the veterinarian for a preliminary SOAP report during a consultation.
 * Holds the selected backgrounds, the generation status and the drafts written by the LLM.
 */
@Entity
public class ClinicalAssistance extends AuditableAbstractAggregateRoot<ClinicalAssistance> {

  @Embedded
  private ClinicalConsultationId consultationId;

  @Embedded
  private PetId petId;

  @Embedded
  private VeterinarianId veterinarianId;

  @Column(nullable = false, columnDefinition = "TEXT")
  private String currentReason;

  // SUBSELECT: two eager lists cannot be join-fetched together (MultipleBagFetchException).
  @ElementCollection(fetch = FetchType.EAGER)
  @Fetch(FetchMode.SUBSELECT)
  @CollectionTable(name = "clinical_assistance_backgrounds",
      joinColumns = @JoinColumn(name = "clinical_assistance_id"))
  private List<SelectedBackground> selectedBackgrounds = new ArrayList<>();

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private AiAnalysisStatus status;

  @Column(length = 500)
  private String failureReason;

  // EAGER because open-in-view is off and the controller reads the drafts after the service.
  @OneToMany(mappedBy = "clinicalAssistance", cascade = CascadeType.ALL, orphanRemoval = true,
      fetch = FetchType.EAGER)
  @Fetch(FetchMode.SUBSELECT)
  @OrderBy("id ASC")
  private List<SoapDraft> drafts = new ArrayList<>();

  protected ClinicalAssistance() {
  }

  public ClinicalAssistance(RequestClinicalAssistanceCommand command) {
    this.consultationId = new ClinicalConsultationId(command.consultationId());
    this.petId = new PetId(command.petId());
    this.veterinarianId = new VeterinarianId(command.veterinarianId());
    this.currentReason = command.currentReason().trim();
    this.selectedBackgrounds.addAll(command.backgrounds());
    this.status = AiAnalysisStatus.PENDING;
  }

  /** Moves to PROCESSING before calling the provider. Used for the first draft and regenerations. */
  public void startGeneration() {
    if (status == AiAnalysisStatus.PROCESSING) {
      throw new InvalidAiAnalysisStateException("A draft is already being generated");
    }
    if (drafts.stream().anyMatch(draft -> draft.getDecision().isConfirmed())) {
      throw new InvalidAiAnalysisStateException("A draft was already confirmed");
    }
    this.status = AiAnalysisStatus.PROCESSING;
    this.failureReason = null;
  }

  public SoapDraft completeWith(String subjective, String objective, String assessment,
      String plan, List<String> warnings, String model) {
    if (status != AiAnalysisStatus.PROCESSING) {
      throw new InvalidAiAnalysisStateException("The assistance is not processing");
    }
    var draft = new SoapDraft(this, subjective, objective, assessment, plan, warnings, model);
    drafts.add(draft);
    this.status = AiAnalysisStatus.COMPLETED;
    registerEvent(new ClinicalAssistanceGeneratedEvent(getId(), consultationId.consultationId()));
    return draft;
  }

  public void fail(String reason) {
    if (status != AiAnalysisStatus.PROCESSING) {
      throw new InvalidAiAnalysisStateException("The assistance is not processing");
    }
    this.status = AiAnalysisStatus.FAILED;
    this.failureReason = reason == null || reason.length() <= 500 ? reason : reason.substring(0, 500);
  }

  /** Accepting or editing a draft discards the other pending drafts. */
  public SoapDraft decideOnDraft(Long draftId, Long requesterId, DraftDecision decision,
      String editedText) {
    if (!belongsTo(requesterId)) {
      throw new ForbiddenException("Only the veterinarian who requested the assistance can decide");
    }
    var draft = findDraft(draftId);
    draft.decide(decision, editedText);
    if (decision != DraftDecision.DISCARDED) {
      drafts.stream()
          .filter(other -> other != draft && other.isPending())
          .forEach(other -> other.decide(DraftDecision.DISCARDED, null));
    }
    return draft;
  }

  public SoapDraft attachReportToDraft(Long draftId, DocumentReference report) {
    var draft = findDraft(draftId);
    draft.attachReport(report);
    return draft;
  }

  public SoapDraft findDraft(Long draftId) {
    return drafts.stream()
        .filter(draft -> Objects.equals(draft.getId(), draftId))
        .findFirst()
        .orElseThrow(() -> new SoapDraftNotFoundException(draftId));
  }

  public boolean belongsTo(Long requesterId) {
    return veterinarianId.veterinarianId().equals(requesterId);
  }

  public List<SelectedBackground> getSelectedBackgrounds() {
    return List.copyOf(selectedBackgrounds);
  }

  public List<SoapDraft> getDrafts() {
    return List.copyOf(drafts);
  }

  public Long getConsultationId() {
    return consultationId.consultationId();
  }

  public Long getPetId() {
    return petId.petId();
  }

  public Long getVeterinarianId() {
    return veterinarianId.veterinarianId();
  }

  public String getCurrentReason() {
    return currentReason;
  }

  public AiAnalysisStatus getStatus() {
    return status;
  }

  public String getFailureReason() {
    return failureReason;
  }
}

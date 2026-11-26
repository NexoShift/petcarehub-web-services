package com.nexoshift.petcarehub.aiassistance.application.internal.commandservices;

import com.nexoshift.petcarehub.aiassistance.application.internal.access.PetAccessGuard;
import com.nexoshift.petcarehub.aiassistance.application.internal.contextbuilders.ClinicalLlmContextBuilder;
import com.nexoshift.petcarehub.aiassistance.application.internal.outboundservices.acl.ExternalClinicalAttentionService;
import com.nexoshift.petcarehub.aiassistance.application.internal.outboundservices.acl.ExternalProfilesService;
import com.nexoshift.petcarehub.aiassistance.application.internal.outboundservices.acl.ExternalSubscriptionsService;
import com.nexoshift.petcarehub.aiassistance.application.internal.outboundservices.acl.PetSummary;
import com.nexoshift.petcarehub.aiassistance.application.internal.outboundservices.llm.LlmService;
import com.nexoshift.petcarehub.aiassistance.application.internal.outboundservices.reports.ClinicalReportData;
import com.nexoshift.petcarehub.aiassistance.application.internal.outboundservices.reports.ClinicalReportRenderer;
import com.nexoshift.petcarehub.aiassistance.application.internal.outboundservices.storage.FileStorageService;
import com.nexoshift.petcarehub.aiassistance.application.internal.prompts.SoapDraftPromptTemplate;
import com.nexoshift.petcarehub.aiassistance.domain.exceptions.AiProviderUnavailableException;
import com.nexoshift.petcarehub.aiassistance.domain.exceptions.ClinicalAssistanceNotFoundException;
import com.nexoshift.petcarehub.aiassistance.domain.model.aggregates.ClinicalAssistance;
import com.nexoshift.petcarehub.aiassistance.domain.model.commands.DecideOnSoapDraftCommand;
import com.nexoshift.petcarehub.aiassistance.domain.model.commands.RegenerateClinicalAssistanceCommand;
import com.nexoshift.petcarehub.aiassistance.domain.model.commands.RequestClinicalAssistanceCommand;
import com.nexoshift.petcarehub.aiassistance.domain.model.valueobjects.ClinicalLlmContext;
import com.nexoshift.petcarehub.aiassistance.domain.model.valueobjects.DocumentReference;
import com.nexoshift.petcarehub.aiassistance.domain.model.valueobjects.DraftDecision;
import com.nexoshift.petcarehub.aiassistance.domain.model.valueobjects.RequesterRole;
import com.nexoshift.petcarehub.aiassistance.domain.repositories.ClinicalAssistanceRepository;
import com.nexoshift.petcarehub.aiassistance.domain.services.ClinicalAssistanceCommandService;
import com.nexoshift.petcarehub.shared.domain.exceptions.BadRequestException;
import com.nexoshift.petcarehub.shared.domain.exceptions.ConflictException;
import com.nexoshift.petcarehub.shared.domain.exceptions.ForbiddenException;
import com.nexoshift.petcarehub.shared.domain.exceptions.NotFoundException;
import java.time.LocalDate;
import java.util.function.Supplier;
import java.time.ZoneId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ClinicalAssistanceCommandServiceImpl implements ClinicalAssistanceCommandService {

  private static final String REPORTS_FOLDER = "reports";
  private static final String PDF = "application/pdf";
  private static final ZoneId LIMA = ZoneId.of("America/Lima");

  private final ClinicalAssistanceRepository repository;
  private final ClinicalLlmContextBuilder contextBuilder;
  private final SoapDraftPromptTemplate promptTemplate;
  private final LlmService llmService;
  private final ExternalSubscriptionsService subscriptionsService;
  private final ExternalProfilesService profilesService;
  private final ClinicalReportRenderer reportRenderer;
  private final FileStorageService fileStorageService;
  private final ExternalClinicalAttentionService clinicalAttentionService;
  private final PetAccessGuard petAccessGuard;

  public ClinicalAssistanceCommandServiceImpl(ClinicalAssistanceRepository repository,
      ClinicalLlmContextBuilder contextBuilder, SoapDraftPromptTemplate promptTemplate,
      LlmService llmService, ExternalSubscriptionsService subscriptionsService,
      ExternalProfilesService profilesService, ClinicalReportRenderer reportRenderer,
      FileStorageService fileStorageService,
      ExternalClinicalAttentionService clinicalAttentionService, PetAccessGuard petAccessGuard) {
    this.repository = repository;
    this.contextBuilder = contextBuilder;
    this.promptTemplate = promptTemplate;
    this.llmService = llmService;
    this.subscriptionsService = subscriptionsService;
    this.profilesService = profilesService;
    this.reportRenderer = reportRenderer;
    this.fileStorageService = fileStorageService;
    this.clinicalAttentionService = clinicalAttentionService;
    this.petAccessGuard = petAccessGuard;
  }

  @Override
  public ClinicalAssistance handle(RequestClinicalAssistanceCommand command) {
    petAccessGuard.ensureCanAccessPet(
        command.petId(), command.veterinarianId(), RequesterRole.VETERINARIAN);
    ensureCurrentConsultationOfPet(command.consultationId(), command.petId());
    // Build the context first: if a background is invalid we fail before spending a credit.
    var context = contextBuilder.build(
        command.petId(), command.currentReason(), command.backgrounds());
    return withLlmCredit(command.veterinarianId(),
        () -> generate(repository.save(new ClinicalAssistance(command)), context));
  }

  @Override
  public ClinicalAssistance handle(RegenerateClinicalAssistanceCommand command) {
    var assistance = load(command.clinicalAssistanceId());
    if (!assistance.belongsTo(command.veterinarianId())) {
      throw new ForbiddenException("Only the veterinarian who requested it can regenerate");
    }
    var context = contextBuilder.build(assistance.getPetId(), assistance.getCurrentReason(),
        assistance.getSelectedBackgrounds());
    return withLlmCredit(command.veterinarianId(), () -> generate(assistance, context));
  }

  /*
   * @Transactional here: building the PDF takes milliseconds, and the decision and the
   * report reference must be saved together. If the database fails after the file was
   * stored, an orphan PDF remains: accepted risk for the sprint (technical debt).
   */
  @Override
  @Transactional
  public ClinicalAssistance handle(DecideOnSoapDraftCommand command) {
    var assistance = load(command.clinicalAssistanceId());
    var draft = assistance.decideOnDraft(command.draftId(), command.veterinarianId(),
        command.decision(), command.editedText());
    if (command.decision().isConfirmed()) {
      var patient = profilesService.fetchPetSummary(assistance.getPetId())
          .map(PetSummary::toPromptText)
          .orElse("Paciente " + assistance.getPetId());
      var pdf = reportRenderer.render(new ClinicalReportData(assistance.getConsultationId(),
          patient, assistance.getCurrentReason(), LocalDate.now(LIMA), draft.finalText(),
          command.decision() == DraftDecision.EDITED, draft.getModel()));
      var stored = fileStorageService.store(pdf, PDF, REPORTS_FOLDER);
      assistance.attachReportToDraft(command.draftId(), new DocumentReference(
          stored.storageKey(), stored.contentType(), stored.sizeBytes()));
    }
    return repository.save(assistance);
  }

  /*
   * No @Transactional here on purpose:
   * 1. The LLM call can take up to 10 s; we do not keep a database transaction open meanwhile.
   * 2. If the provider fails we want the FAILED status saved. Inside a transaction, the
   *    exception would roll it back.
   */
  private ClinicalAssistance generate(ClinicalAssistance assistance, ClinicalLlmContext context) {
    assistance.startGeneration();
    assistance = repository.save(assistance);
    try {
      var completion = llmService.generateSoapDraft(promptTemplate.render(context));
      assistance.completeWith(completion.subjective(), completion.objective(),
          completion.assessment(), completion.plan(), completion.warnings(), completion.model());
      return repository.save(assistance);
    } catch (AiProviderUnavailableException | IllegalArgumentException exception) {
      // IllegalArgumentException: the model returned an empty SOAP section.
      assistance.fail(exception.getMessage());
      repository.save(assistance);
      throw exception instanceof AiProviderUnavailableException unavailable
          ? unavailable
          : new AiProviderUnavailableException("LLM", exception);
    }
  }

  /**
   * Reserves one LLM credit of the clinic's plan, runs the generation, and confirms the
   * credit when the model answered. If anything fails, the credit is released.
   * Without an active subscription: 403. Without credits left in the period: 409.
   */
  private ClinicalAssistance withLlmCredit(Long veterinarianUserId,
                                           Supplier<ClinicalAssistance> generation) {
    var reservationId = subscriptionsService.reserveLlmCredit(veterinarianUserId);
    try {
      var result = generation.get();
      subscriptionsService.confirmCredit(reservationId);
      return result;
    } catch (RuntimeException exception) {
      subscriptionsService.releaseCredit(reservationId);
      throw exception;
    }
  }

  /** The draft is for the consultation in progress, and it must be of the same pet. */
  private void ensureCurrentConsultationOfPet(Long consultationId, Long petId) {
    var consultationPetId = clinicalAttentionService.fetchPetIdByConsultationId(consultationId)
        .orElseThrow(() -> new NotFoundException(
            "Clinical consultation " + consultationId + " was not found"));
    if (!consultationPetId.equals(petId)) {
      throw new BadRequestException("The consultation is not of this pet");
    }
    if (!clinicalAttentionService.isConsultationInProgress(consultationId)) {
      throw new ConflictException("The consultation must be IN_PROGRESS to request a draft");
    }
  }

  private ClinicalAssistance load(Long id) {
    return repository.findById(id)
        .orElseThrow(() -> new ClinicalAssistanceNotFoundException(id));
  }
}

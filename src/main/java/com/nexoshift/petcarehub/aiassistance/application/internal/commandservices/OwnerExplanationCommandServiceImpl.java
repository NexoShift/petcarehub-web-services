package com.nexoshift.petcarehub.aiassistance.application.internal.commandservices;

import com.nexoshift.petcarehub.aiassistance.domain.model.aggregates.OwnerExplanation;
import com.nexoshift.petcarehub.aiassistance.domain.model.commands.RequestOwnerExplanationCommand;
import com.nexoshift.petcarehub.aiassistance.domain.model.valueobjects.PetId;
import com.nexoshift.petcarehub.aiassistance.domain.model.valueobjects.UserId;
import com.nexoshift.petcarehub.aiassistance.domain.repositories.OwnerExplanationRepository;
import com.nexoshift.petcarehub.aiassistance.domain.services.OwnerExplanationCommandService;
import com.nexoshift.petcarehub.aiassistance.application.internal.outboundservices.acl.ConsultationBackground;
import com.nexoshift.petcarehub.aiassistance.application.internal.outboundservices.acl.ExternalClinicalAttentionService;
import com.nexoshift.petcarehub.aiassistance.application.internal.outboundservices.acl.ExternalProfilesService;
import com.nexoshift.petcarehub.aiassistance.application.internal.outboundservices.acl.ExternalSubscriptionsService;
import com.nexoshift.petcarehub.aiassistance.application.internal.outboundservices.llm.LlmService;
import com.nexoshift.petcarehub.aiassistance.application.internal.prompts.OwnerExplanationPromptTemplate;
import com.nexoshift.petcarehub.shared.domain.exceptions.BadRequestException;
import com.nexoshift.petcarehub.shared.domain.exceptions.ForbiddenException;
import com.nexoshift.petcarehub.shared.domain.exceptions.NotFoundException;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class OwnerExplanationCommandServiceImpl implements OwnerExplanationCommandService {

  private static final int MAX_CONSULTATIONS = 3;
  private static final int MAX_PREVIOUS_TURNS = 3;

  private final OwnerExplanationRepository repository;
  private final ExternalProfilesService profilesService;
  private final ExternalClinicalAttentionService clinicalAttentionService;
  private final ExternalSubscriptionsService subscriptionsService;
  private final OwnerExplanationPromptTemplate promptTemplate;
  private final LlmService llmService;

  public OwnerExplanationCommandServiceImpl(OwnerExplanationRepository repository,
      ExternalProfilesService profilesService,
      ExternalClinicalAttentionService clinicalAttentionService,
      ExternalSubscriptionsService subscriptionsService,
      OwnerExplanationPromptTemplate promptTemplate, LlmService llmService) {
    this.repository = repository;
    this.profilesService = profilesService;
    this.clinicalAttentionService = clinicalAttentionService;
    this.subscriptionsService = subscriptionsService;
    this.promptTemplate = promptTemplate;
    this.llmService = llmService;
  }

  /** No @Transactional: the model call can take up to 10 s. Nothing is saved if it fails. */
  @Override
  public OwnerExplanation handle(RequestOwnerExplanationCommand command) {
    var pet = profilesService.fetchPetSummary(command.petId())
        .orElseThrow(() -> new NotFoundException("Pet " + command.petId() + " was not found"));
    if (!profilesService.isPetOwnedBy(command.petId(), command.ownerUserId())) {
      throw new ForbiddenException("You can only ask about your own pets");
    }
    subscriptionsService.ensureAiUsageAllowed(command.ownerUserId());
    var consultations = consultationsFor(command);
    var history = repository.findByPetIdAndOwnerIdOrderByIdAsc(
        new PetId(command.petId()), new UserId(command.ownerUserId()));
    var previousTurns = history.subList(Math.max(0, history.size() - MAX_PREVIOUS_TURNS),
        history.size());
    var answer = llmService.answerOwnerQuestion(
        promptTemplate.render(pet, consultations, previousTurns, command.question()));
    return repository.save(new OwnerExplanation(command, answer.text(), answer.model()));
  }

  /** The referenced consultation, or the latest ones of the pet. Never another pet's data. */
  private List<ConsultationBackground> consultationsFor(RequestOwnerExplanationCommand command) {
    if (command.consultationId() != null) {
      var consultation = clinicalAttentionService.fetchConsultation(command.consultationId())
          .filter(found -> found.petId().equals(command.petId()))
          .orElseThrow(() -> new BadRequestException(
              "Consultation " + command.consultationId() + " is not of this pet"));
      return List.of(consultation);
    }
    var all = clinicalAttentionService.fetchConsultationsByPetId(command.petId());
    return all.subList(0, Math.min(MAX_CONSULTATIONS, all.size()));
  }
}

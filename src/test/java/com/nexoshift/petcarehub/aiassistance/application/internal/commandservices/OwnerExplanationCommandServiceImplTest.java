package com.nexoshift.petcarehub.aiassistance.application.internal.commandservices;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.nexoshift.petcarehub.aiassistance.application.internal.outboundservices.acl.ConsultationBackground;
import com.nexoshift.petcarehub.aiassistance.application.internal.outboundservices.acl.ExternalClinicalAttentionService;
import com.nexoshift.petcarehub.aiassistance.application.internal.outboundservices.acl.ExternalProfilesService;
import com.nexoshift.petcarehub.aiassistance.application.internal.outboundservices.acl.ExternalSubscriptionsService;
import com.nexoshift.petcarehub.aiassistance.application.internal.outboundservices.acl.PetSummary;
import com.nexoshift.petcarehub.aiassistance.application.internal.outboundservices.llm.LlmAnswer;
import com.nexoshift.petcarehub.aiassistance.application.internal.outboundservices.llm.LlmPrompt;
import com.nexoshift.petcarehub.aiassistance.application.internal.outboundservices.llm.LlmService;
import com.nexoshift.petcarehub.aiassistance.application.internal.prompts.OwnerExplanationPromptTemplate;
import com.nexoshift.petcarehub.aiassistance.domain.exceptions.AiProviderUnavailableException;
import com.nexoshift.petcarehub.aiassistance.domain.model.aggregates.OwnerExplanation;
import com.nexoshift.petcarehub.aiassistance.domain.model.commands.RequestOwnerExplanationCommand;
import com.nexoshift.petcarehub.aiassistance.domain.repositories.OwnerExplanationRepository;
import com.nexoshift.petcarehub.shared.domain.exceptions.BadRequestException;
import com.nexoshift.petcarehub.shared.domain.exceptions.ForbiddenException;
import com.nexoshift.petcarehub.shared.domain.exceptions.NotFoundException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class OwnerExplanationCommandServiceImplTest {

  private static final PetSummary ROCKY =
      new PetSummary(1L, "Rocky", "Perro", "Bulldog", "macho", 38, "atigrado");

  @Mock OwnerExplanationRepository repository;
  @Mock ExternalProfilesService profilesService;
  @Mock ExternalClinicalAttentionService clinicalAttentionService;
  @Mock ExternalSubscriptionsService subscriptionsService;
  @Mock LlmService llmService;

  private OwnerExplanationCommandServiceImpl service;

  @BeforeEach
  void setUp() {
    service = new OwnerExplanationCommandServiceImpl(repository, profilesService,
        clinicalAttentionService, subscriptionsService, new OwnerExplanationPromptTemplate(),
        llmService);
    when(profilesService.fetchPetSummary(1L)).thenReturn(Optional.of(ROCKY));
    when(profilesService.isPetOwnedBy(1L, 9L)).thenReturn(true);
    when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
    when(llmService.answerOwnerQuestion(any())).thenReturn(new LlmAnswer("Es una alergia.", "m"));
  }

  private static ConsultationBackground consultation(long id, long petId) {
    return new ConsultationBackground(id, petId, LocalDate.of(2026, 3, (int) id),
        "Motivo " + id, "Hallazgo " + id, "Diagnóstico " + id, "Tratamiento " + id);
  }

  private LlmPrompt sentPrompt() {
    var captor = ArgumentCaptor.forClass(LlmPrompt.class);
    verify(llmService).answerOwnerQuestion(captor.capture());
    return captor.getValue();
  }

  @Test
  void usesOnlyTheLatestThreeConsultationsAndSavesTheAnswer() {
    when(clinicalAttentionService.fetchConsultationsByPetId(1L)).thenReturn(List.of(
        consultation(4, 1), consultation(3, 1), consultation(2, 1), consultation(1, 1)));

    var explanation = service.handle(new RequestOwnerExplanationCommand(1L, null, 9L, "¿Qué es?"));

    assertEquals("Es una alergia.", explanation.getAnswer());
    var prompt = sentPrompt().userMessage();
    assertTrue(prompt.contains("Diagnóstico 2"));
    assertFalse(prompt.contains("Diagnóstico 1"));
  }

  @Test
  void referencedConsultationIsTheOnlyContext() {
    when(clinicalAttentionService.fetchConsultation(2L)).thenReturn(Optional.of(consultation(2, 1)));

    service.handle(new RequestOwnerExplanationCommand(1L, 2L, 9L, "¿Qué es?"));

    var prompt = sentPrompt().userMessage();
    assertTrue(prompt.contains("Diagnóstico 2"));
    verify(clinicalAttentionService, never()).fetchConsultationsByPetId(any());
  }

  @Test
  void consultationOfAnotherPetIsRejected() {
    when(clinicalAttentionService.fetchConsultation(5L)).thenReturn(Optional.of(consultation(5, 2)));
    assertThrows(BadRequestException.class,
        () -> service.handle(new RequestOwnerExplanationCommand(1L, 5L, 9L, "¿Qué es?")));
    verifyNoInteractions(llmService);
  }

  @Test
  void sendsOnlyTheLastThreeTurns() {
    var history = new ArrayList<OwnerExplanation>();
    for (int turn = 1; turn <= 5; turn++) {
      history.add(new OwnerExplanation(
          new RequestOwnerExplanationCommand(1L, null, 9L, "Pregunta " + turn), "R", "m"));
    }
    when(repository.findByPetIdAndOwnerIdOrderByIdAsc(any(), any())).thenReturn(history);

    service.handle(new RequestOwnerExplanationCommand(1L, null, 9L, "¿Y ahora?"));

    var prompt = sentPrompt().userMessage();
    assertFalse(prompt.contains("Pregunta 2"));
    assertTrue(prompt.contains("Pregunta 3"));
    assertTrue(prompt.contains("Pregunta 5"));
  }

  @Test
  void unknownPetIsNotFoundAndForeignPetIsForbidden() {
    assertThrows(NotFoundException.class,
        () -> service.handle(new RequestOwnerExplanationCommand(99L, null, 9L, "¿Qué es?")));
    assertThrows(ForbiddenException.class,
        () -> service.handle(new RequestOwnerExplanationCommand(1L, null, 10L, "¿Qué es?")));
    verifyNoInteractions(llmService);
  }

  @Test
  void nothingIsSavedWhenTheModelFails() {
    when(llmService.answerOwnerQuestion(any()))
        .thenThrow(new AiProviderUnavailableException("fake", null));

    assertThrows(AiProviderUnavailableException.class,
        () -> service.handle(new RequestOwnerExplanationCommand(1L, null, 9L, "¿Qué es?")));
    verify(repository, never()).save(any());
  }
}

package com.nexoshift.petcarehub.aiassistance.application.internal.commandservices;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.nexoshift.petcarehub.aiassistance.application.internal.access.PetAccessGuard;
import com.nexoshift.petcarehub.aiassistance.application.internal.contextbuilders.ClinicalLlmContextBuilder;
import com.nexoshift.petcarehub.aiassistance.application.internal.outboundservices.acl.ExternalClinicalAttentionService;
import com.nexoshift.petcarehub.aiassistance.application.internal.outboundservices.acl.ExternalProfilesService;
import com.nexoshift.petcarehub.aiassistance.application.internal.outboundservices.acl.ExternalSubscriptionsService;
import com.nexoshift.petcarehub.aiassistance.application.internal.outboundservices.llm.LlmCompletion;
import com.nexoshift.petcarehub.aiassistance.application.internal.outboundservices.llm.LlmService;
import com.nexoshift.petcarehub.aiassistance.application.internal.outboundservices.reports.ClinicalReportRenderer;
import com.nexoshift.petcarehub.aiassistance.application.internal.outboundservices.storage.FileStorageService;
import com.nexoshift.petcarehub.aiassistance.application.internal.outboundservices.storage.StoredFile;
import com.nexoshift.petcarehub.aiassistance.application.internal.prompts.SoapDraftPromptTemplate;
import com.nexoshift.petcarehub.aiassistance.domain.exceptions.AiProviderUnavailableException;
import com.nexoshift.petcarehub.aiassistance.domain.exceptions.ClinicalAssistanceNotFoundException;
import com.nexoshift.petcarehub.aiassistance.domain.model.aggregates.ClinicalAssistance;
import com.nexoshift.petcarehub.aiassistance.domain.model.commands.DecideOnSoapDraftCommand;
import com.nexoshift.petcarehub.aiassistance.domain.model.commands.RegenerateClinicalAssistanceCommand;
import com.nexoshift.petcarehub.aiassistance.domain.model.commands.RequestClinicalAssistanceCommand;
import com.nexoshift.petcarehub.aiassistance.domain.model.valueobjects.AiAnalysisStatus;
import com.nexoshift.petcarehub.aiassistance.domain.model.valueobjects.ClinicalLlmContext;
import com.nexoshift.petcarehub.aiassistance.domain.model.valueobjects.DraftDecision;
import com.nexoshift.petcarehub.aiassistance.domain.repositories.ClinicalAssistanceRepository;
import com.nexoshift.petcarehub.shared.domain.exceptions.ForbiddenException;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class ClinicalAssistanceCommandServiceImplTest {

  private static final Long VET_ID = 7L;

  @Mock ClinicalAssistanceRepository repository;
  @Mock ClinicalLlmContextBuilder contextBuilder;
  @Mock LlmService llmService;
  @Mock ExternalSubscriptionsService subscriptionsService;
  @Mock ExternalProfilesService profilesService;
  @Mock ClinicalReportRenderer reportRenderer;
  @Mock FileStorageService fileStorageService;
  @Mock ExternalClinicalAttentionService clinicalAttentionService;
  @Mock PetAccessGuard petAccessGuard;

  private ClinicalAssistanceCommandServiceImpl service;

  @BeforeEach
  void setUp() {
    service = new ClinicalAssistanceCommandServiceImpl(repository, contextBuilder,
        new SoapDraftPromptTemplate(), llmService, subscriptionsService, profilesService,
        reportRenderer, fileStorageService, clinicalAttentionService, petAccessGuard);
    // Consultation 4 is the consultation IN_PROGRESS of pet 1.
    lenient().when(clinicalAttentionService.fetchPetIdByConsultationId(4L))
        .thenReturn(Optional.of(1L));
    lenient().when(clinicalAttentionService.isConsultationInProgress(4L)).thenReturn(true);
  }

  private static RequestClinicalAssistanceCommand request() {
    return new RequestClinicalAssistanceCommand(4L, 1L, VET_ID, "motivo", List.of());
  }

  private static LlmCompletion completion() {
    return new LlmCompletion("s", "o", "a", "p", List.of("w"), "gpt-5.4-mini");
  }

  private ClinicalAssistance completedAssistance(long draftId) {
    var assistance = new ClinicalAssistance(request());
    assistance.startGeneration();
    var draft = assistance.completeWith("s", "o", "a", "p", List.of(), "fake");
    ReflectionTestUtils.setField(draft, "id", draftId);
    return assistance;
  }

  @Test
  void successfulGenerationCompletesWithOneDraft() {
    when(contextBuilder.build(any(), any(), any()))
        .thenReturn(new ClinicalLlmContext("Perro", "motivo", List.of()));
    when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
    when(llmService.generateSoapDraft(any())).thenReturn(completion());

    var assistance = service.handle(request());

    assertEquals(AiAnalysisStatus.COMPLETED, assistance.getStatus());
    assertEquals(1, assistance.getDrafts().size());
    assertEquals("gpt-5.4-mini", assistance.getDrafts().getFirst().getModel());
    verify(subscriptionsService).reserveLlmCredit(VET_ID);
    verify(subscriptionsService).confirmCredit(any());
    verify(subscriptionsService, never()).releaseCredit(any());
  }

  @Test
  void providerFailureIsSavedAsFailedAndRethrown() {
    when(contextBuilder.build(any(), any(), any()))
        .thenReturn(new ClinicalLlmContext("Perro", "motivo", List.of()));
    when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
    when(llmService.generateSoapDraft(any()))
        .thenThrow(new AiProviderUnavailableException("test", null));

    assertThrows(AiProviderUnavailableException.class, () -> service.handle(request()));
    verify(repository, atLeastOnce()).save(argThat(a -> a.getStatus() == AiAnalysisStatus.FAILED));
    verify(subscriptionsService).releaseCredit(any());
    verify(subscriptionsService, never()).confirmCredit(any());
  }

  @Test
  void emptySectionFromTheModelIsAProviderFailure() {
    when(contextBuilder.build(any(), any(), any()))
        .thenReturn(new ClinicalLlmContext("Perro", "motivo", List.of()));
    when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
    when(llmService.generateSoapDraft(any()))
        .thenReturn(new LlmCompletion("s", "", "a", "p", List.of(), "m"));

    assertThrows(AiProviderUnavailableException.class, () -> service.handle(request()));
    verify(repository, atLeastOnce()).save(argThat(a -> a.getStatus() == AiAnalysisStatus.FAILED));
  }

  @Test
  void invalidBackgroundFailsBeforeSavingAnything() {
    when(contextBuilder.build(any(), any(), any()))
        .thenThrow(new com.nexoshift.petcarehub.shared.domain.exceptions.BadRequestException("x"));

    assertThrows(RuntimeException.class, () -> service.handle(request()));
    verify(repository, never()).save(any());
    verifyNoInteractions(llmService);
  }

  @Test
  void onlyTheRequesterCanRegenerate() {
    when(repository.findById(1L)).thenReturn(Optional.of(completedAssistance(10L)));

    assertThrows(ForbiddenException.class,
        () -> service.handle(new RegenerateClinicalAssistanceCommand(1L, 99L)));
    verifyNoInteractions(llmService);
  }

  @Test
  void unknownAssistanceIsNotFound() {
    when(repository.findById(1L)).thenReturn(Optional.empty());
    assertThrows(ClinicalAssistanceNotFoundException.class,
        () -> service.handle(new RegenerateClinicalAssistanceCommand(1L, VET_ID)));
  }

  @Test
  void acceptingADraftStoresThePdfReport() {
    when(repository.findById(1L)).thenReturn(Optional.of(completedAssistance(10L)));
    when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
    when(profilesService.fetchPetSummary(1L)).thenReturn(Optional.empty());
    when(reportRenderer.render(any())).thenReturn(new byte[] {1, 2, 3});
    when(fileStorageService.store(any(), eq("application/pdf"), eq("reports")))
        .thenReturn(new StoredFile("reports/2026/11/a.pdf", "application/pdf", 3));

    var assistance = service.handle(
        new DecideOnSoapDraftCommand(1L, 10L, VET_ID, DraftDecision.ACCEPTED, null));

    var draft = assistance.findDraft(10L);
    assertTrue(draft.hasReport());
    assertEquals("reports/2026/11/a.pdf", draft.getReport().storageKey());
  }

  @Test
  void discardingADraftDoesNotCreateAReport() {
    when(repository.findById(1L)).thenReturn(Optional.of(completedAssistance(10L)));
    when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

    service.handle(new DecideOnSoapDraftCommand(1L, 10L, VET_ID, DraftDecision.DISCARDED, null));

    verifyNoInteractions(reportRenderer, fileStorageService);
  }
}

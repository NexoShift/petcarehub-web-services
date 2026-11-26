package com.nexoshift.petcarehub.aiassistance.domain.model.aggregates;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.nexoshift.petcarehub.aiassistance.domain.exceptions.InvalidAiAnalysisStateException;
import com.nexoshift.petcarehub.aiassistance.domain.exceptions.SoapDraftNotFoundException;
import com.nexoshift.petcarehub.aiassistance.domain.model.commands.RequestClinicalAssistanceCommand;
import com.nexoshift.petcarehub.aiassistance.domain.model.entities.SoapDraft;
import com.nexoshift.petcarehub.aiassistance.domain.model.events.ClinicalAssistanceGeneratedEvent;
import com.nexoshift.petcarehub.aiassistance.domain.model.valueobjects.AiAnalysisStatus;
import com.nexoshift.petcarehub.aiassistance.domain.model.valueobjects.BackgroundType;
import com.nexoshift.petcarehub.aiassistance.domain.model.valueobjects.DocumentReference;
import com.nexoshift.petcarehub.aiassistance.domain.model.valueobjects.DraftDecision;
import com.nexoshift.petcarehub.aiassistance.domain.model.valueobjects.SelectedBackground;
import com.nexoshift.petcarehub.shared.domain.exceptions.ForbiddenException;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

/** Domain rules, without Spring. Draft ids are set by hand because only the database sets them. */
class ClinicalAssistanceTest {

  private static final Long VET_ID = 7L;

  private ClinicalAssistance newAssistance() {
    return new ClinicalAssistance(new RequestClinicalAssistanceCommand(4L, 1L, VET_ID,
        "  Rascado en orejas  ",
        List.of(new SelectedBackground(BackgroundType.PREVIOUS_CONSULTATION, 1L))));
  }

  private SoapDraft addDraft(ClinicalAssistance assistance, long draftId) {
    assistance.startGeneration();
    var draft = assistance.completeWith("s", "o", "a", "p", List.of("w1", "w2"), "fake");
    ReflectionTestUtils.setField(draft, "id", draftId);
    return draft;
  }

  @Test
  void newAssistanceIsPendingWithTrimmedReason() {
    var assistance = newAssistance();
    assertEquals(AiAnalysisStatus.PENDING, assistance.getStatus());
    assertEquals("Rascado en orejas", assistance.getCurrentReason());
    assertEquals(1, assistance.getSelectedBackgrounds().size());
  }

  @Test
  void cannotCompleteWhenNotProcessing() {
    var assistance = newAssistance();
    assertThrows(InvalidAiAnalysisStateException.class,
        () -> assistance.completeWith("s", "o", "a", "p", List.of(), "fake"));
  }

  @Test
  void cannotStartTwoGenerationsAtTheSameTime() {
    var assistance = newAssistance();
    assistance.startGeneration();
    assertThrows(InvalidAiAnalysisStateException.class, assistance::startGeneration);
  }

  @Test
  void completingAddsAPendingDraftAndRegistersAnEvent() {
    var assistance = newAssistance();
    var draft = addDraft(assistance, 10L);
    assertEquals(AiAnalysisStatus.COMPLETED, assistance.getStatus());
    assertEquals(DraftDecision.PENDING, draft.getDecision());
    assertEquals(List.of("w1", "w2"), draft.getWarnings());
    var events = (java.util.Collection<?>) ReflectionTestUtils.invokeMethod(
        assistance, "domainEvents");
    assertTrue(events.stream().anyMatch(ClinicalAssistanceGeneratedEvent.class::isInstance));
  }

  @Test
  void emptySoapSectionIsRejected() {
    var assistance = newAssistance();
    assistance.startGeneration();
    assertThrows(IllegalArgumentException.class,
        () -> assistance.completeWith("s", " ", "a", "p", List.of(), "fake"));
  }

  @Test
  void failingKeepsTheReasonAndAllowsRetrying() {
    var assistance = newAssistance();
    assistance.startGeneration();
    assistance.fail("timeout");
    assertEquals(AiAnalysisStatus.FAILED, assistance.getStatus());
    assertEquals("timeout", assistance.getFailureReason());
    assistance.startGeneration();
    assertEquals(AiAnalysisStatus.PROCESSING, assistance.getStatus());
    assertNull(assistance.getFailureReason());
  }

  @Test
  void onlyRequesterCanDecide() {
    var assistance = newAssistance();
    addDraft(assistance, 10L);
    assertThrows(ForbiddenException.class,
        () -> assistance.decideOnDraft(10L, 99L, DraftDecision.ACCEPTED, null));
  }

  @Test
  void unknownDraftIsNotFound() {
    var assistance = newAssistance();
    addDraft(assistance, 10L);
    assertThrows(SoapDraftNotFoundException.class,
        () -> assistance.decideOnDraft(11L, VET_ID, DraftDecision.ACCEPTED, null));
  }

  @Test
  void acceptingOneDraftDiscardsOtherPendingDrafts() {
    var assistance = newAssistance();
    var first = addDraft(assistance, 10L);
    var second = addDraft(assistance, 11L);
    assistance.decideOnDraft(11L, VET_ID, DraftDecision.ACCEPTED, null);
    assertEquals(DraftDecision.ACCEPTED, second.getDecision());
    assertEquals(DraftDecision.DISCARDED, first.getDecision());
  }

  @Test
  void discardingOneDraftKeepsTheOthersPending() {
    var assistance = newAssistance();
    var first = addDraft(assistance, 10L);
    addDraft(assistance, 11L);
    assistance.decideOnDraft(11L, VET_ID, DraftDecision.DISCARDED, null);
    assertTrue(first.isPending());
  }

  @Test
  void aDraftCannotBeDecidedTwice() {
    var assistance = newAssistance();
    addDraft(assistance, 10L);
    assistance.decideOnDraft(10L, VET_ID, DraftDecision.ACCEPTED, null);
    assertThrows(InvalidAiAnalysisStateException.class,
        () -> assistance.decideOnDraft(10L, VET_ID, DraftDecision.DISCARDED, null));
  }

  @Test
  void cannotRegenerateAfterAConfirmedDraft() {
    var assistance = newAssistance();
    addDraft(assistance, 10L);
    assistance.decideOnDraft(10L, VET_ID, DraftDecision.ACCEPTED, null);
    assertThrows(InvalidAiAnalysisStateException.class, assistance::startGeneration);
  }

  @Test
  void editedDraftUsesTheEditedTextAsFinalText() {
    var assistance = newAssistance();
    var draft = addDraft(assistance, 10L);
    assistance.decideOnDraft(10L, VET_ID, DraftDecision.EDITED, "  Texto final  ");
    assertEquals("Texto final", draft.finalText());
  }

  @Test
  void acceptedDraftUsesTheSoapSectionsAsFinalText() {
    var assistance = newAssistance();
    var draft = addDraft(assistance, 10L);
    assistance.decideOnDraft(10L, VET_ID, DraftDecision.ACCEPTED, null);
    assertTrue(draft.finalText().startsWith("S: s"));
    assertTrue(draft.finalText().contains("P: p"));
  }

  @Test
  void onlyConfirmedDraftsGetAReport() {
    var assistance = newAssistance();
    var draft = addDraft(assistance, 10L);
    var report = new DocumentReference("reports/2026/11/x.pdf", "application/pdf", 10L);
    assertThrows(InvalidAiAnalysisStateException.class,
        () -> assistance.attachReportToDraft(10L, report));
    assistance.decideOnDraft(10L, VET_ID, DraftDecision.ACCEPTED, null);
    assistance.attachReportToDraft(10L, report);
    assertTrue(draft.hasReport());
    assertFalse(draft.isPending());
  }
}

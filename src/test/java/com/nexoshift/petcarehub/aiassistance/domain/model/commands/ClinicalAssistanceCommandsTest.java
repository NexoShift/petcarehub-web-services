package com.nexoshift.petcarehub.aiassistance.domain.model.commands;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.nexoshift.petcarehub.aiassistance.domain.model.valueobjects.BackgroundType;
import com.nexoshift.petcarehub.aiassistance.domain.model.valueobjects.DraftDecision;
import com.nexoshift.petcarehub.aiassistance.domain.model.valueobjects.PetId;
import com.nexoshift.petcarehub.aiassistance.domain.model.valueobjects.SelectedBackground;
import java.util.List;
import java.util.stream.LongStream;
import org.junit.jupiter.api.Test;

class ClinicalAssistanceCommandsTest {

  @Test
  void duplicatedBackgroundIsRejected() {
    var background = new SelectedBackground(BackgroundType.PREVIOUS_CONSULTATION, 1L);
    assertThrows(IllegalArgumentException.class, () -> new RequestClinicalAssistanceCommand(
        4L, 1L, 7L, "motivo", List.of(background, background)));
  }

  @Test
  void moreThanTenBackgroundsAreRejected() {
    var backgrounds = LongStream.rangeClosed(1, 11)
        .mapToObj(id -> new SelectedBackground(BackgroundType.PREVIOUS_CONSULTATION, id))
        .toList();
    assertThrows(IllegalArgumentException.class,
        () -> new RequestClinicalAssistanceCommand(4L, 1L, 7L, "motivo", backgrounds));
  }

  @Test
  void blankReasonIsRejected() {
    assertThrows(IllegalArgumentException.class,
        () -> new RequestClinicalAssistanceCommand(4L, 1L, 7L, " ", List.of()));
  }

  @Test
  void editedDecisionRequiresText() {
    assertThrows(IllegalArgumentException.class,
        () -> new DecideOnSoapDraftCommand(1L, 1L, 7L, DraftDecision.EDITED, " "));
  }

  @Test
  void pendingIsNotAValidDecision() {
    assertThrows(IllegalArgumentException.class,
        () -> new DecideOnSoapDraftCommand(1L, 1L, 7L, DraftDecision.PENDING, null));
    assertDoesNotThrow(
        () -> new DecideOnSoapDraftCommand(1L, 1L, 7L, DraftDecision.ACCEPTED, null));
  }

  @Test
  void idsMustBePositive() {
    assertThrows(IllegalArgumentException.class, () -> new PetId(0L));
    assertThrows(IllegalArgumentException.class,
        () -> new SelectedBackground(BackgroundType.PREVIOUS_CONSULTATION, -1L));
  }
}

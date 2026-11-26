package com.nexoshift.petcarehub.aiassistance.domain.model.commands;

import com.nexoshift.petcarehub.aiassistance.domain.model.valueobjects.DraftDecision;

public record DecideOnSoapDraftCommand(
    Long clinicalAssistanceId,
    Long draftId,
    Long veterinarianId,
    DraftDecision decision,
    String editedText) {

  public DecideOnSoapDraftCommand {
    if (decision == null || decision == DraftDecision.PENDING) {
      throw new IllegalArgumentException("Decision must be ACCEPTED, EDITED or DISCARDED");
    }
    if (decision == DraftDecision.EDITED && (editedText == null || editedText.isBlank())) {
      throw new IllegalArgumentException("Edited text is required when the decision is EDITED");
    }
  }
}

package com.nexoshift.petcarehub.aiassistance.interfaces.rest.transform;

import com.nexoshift.petcarehub.aiassistance.domain.model.commands.DecideOnSoapDraftCommand;
import com.nexoshift.petcarehub.aiassistance.interfaces.rest.resources.DraftDecisionResource;

public final class DecideOnSoapDraftCommandFromResourceAssembler {

  private DecideOnSoapDraftCommandFromResourceAssembler() {
  }

  public static DecideOnSoapDraftCommand toCommandFromResource(Long clinicalAssistanceId,
      Long draftId, Long veterinarianId, DraftDecisionResource resource) {
    return new DecideOnSoapDraftCommand(clinicalAssistanceId, draftId, veterinarianId,
        resource.decision(), resource.editedText());
  }
}

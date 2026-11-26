package com.nexoshift.petcarehub.aiassistance.domain.model.commands;

import com.nexoshift.petcarehub.aiassistance.domain.model.valueobjects.SelectedBackground;
import java.util.List;

public record RequestClinicalAssistanceCommand(
    Long consultationId,
    Long petId,
    Long veterinarianId,
    String currentReason,
    List<SelectedBackground> backgrounds) {

  private static final int MAX_BACKGROUNDS = 10;

  public RequestClinicalAssistanceCommand {
    if (currentReason == null || currentReason.isBlank()) {
      throw new IllegalArgumentException("Current reason is required");
    }
    backgrounds = backgrounds == null ? List.of() : List.copyOf(backgrounds);
    if (backgrounds.size() > MAX_BACKGROUNDS) {
      throw new IllegalArgumentException("Select at most " + MAX_BACKGROUNDS + " backgrounds");
    }
    if (backgrounds.stream().distinct().count() != backgrounds.size()) {
      throw new IllegalArgumentException("A background was selected twice");
    }
  }
}

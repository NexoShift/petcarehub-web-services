package com.nexoshift.petcarehub.clinicalattention.domain.exceptions;

import com.nexoshift.petcarehub.shared.domain.exceptions.ConflictException;
import java.util.List;

/**
 * The consultation cannot be completed because required data is missing (HTTP 409). The
 * consultation is left untouched and the missing items are reported.
 */
public class IncompleteConsultationException extends ConflictException {

  private final List<String> pendingItems;

  public IncompleteConsultationException(List<String> pendingItems) {
    super("The clinical consultation cannot be completed. Pending: "
        + String.join(", ", pendingItems));
    this.pendingItems = List.copyOf(pendingItems);
  }

  /** Names of the missing data: anamnesis, physiologicalAssessment and primaryDiagnosis. */
  public List<String> getPendingItems() {
    return pendingItems;
  }
}

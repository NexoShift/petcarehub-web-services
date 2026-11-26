package com.nexoshift.petcarehub.aiassistance.domain.services;

import com.nexoshift.petcarehub.aiassistance.domain.model.aggregates.OwnerExplanation;
import com.nexoshift.petcarehub.aiassistance.domain.model.commands.RequestOwnerExplanationCommand;

public interface OwnerExplanationCommandService {

  OwnerExplanation handle(RequestOwnerExplanationCommand command);
}

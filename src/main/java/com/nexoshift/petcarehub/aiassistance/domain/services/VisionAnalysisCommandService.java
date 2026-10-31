package com.nexoshift.petcarehub.aiassistance.domain.services;

import com.nexoshift.petcarehub.aiassistance.domain.model.aggregates.VisionAnalysis;
import com.nexoshift.petcarehub.aiassistance.domain.model.commands.RequestVisionAnalysisCommand;

public interface VisionAnalysisCommandService {

  VisionAnalysis handle(RequestVisionAnalysisCommand command);
}

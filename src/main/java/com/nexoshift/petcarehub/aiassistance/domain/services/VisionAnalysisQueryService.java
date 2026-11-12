package com.nexoshift.petcarehub.aiassistance.domain.services;

import com.nexoshift.petcarehub.aiassistance.domain.model.aggregates.VisionAnalysis;
import com.nexoshift.petcarehub.aiassistance.domain.model.queries.GetVisionAnalysesByConsultationIdQuery;
import com.nexoshift.petcarehub.aiassistance.domain.model.queries.GetVisionAnalysesByPetIdQuery;
import com.nexoshift.petcarehub.aiassistance.domain.model.queries.GetVisionAnalysisByIdQuery;
import java.util.List;
import java.util.Optional;

public interface VisionAnalysisQueryService {

  Optional<VisionAnalysis> handle(GetVisionAnalysisByIdQuery query);

  List<VisionAnalysis> handle(GetVisionAnalysesByPetIdQuery query);

  List<VisionAnalysis> handle(GetVisionAnalysesByConsultationIdQuery query);
}

package com.nexoshift.petcarehub.clinicalexams.domain.services;

import com.nexoshift.petcarehub.clinicalexams.domain.model.aggregates.ClinicalExam;
import com.nexoshift.petcarehub.clinicalexams.domain.model.queries.GetClinicalExamByIdQuery;
import com.nexoshift.petcarehub.clinicalexams.domain.model.queries.GetClinicalExamsByConsultationIdQuery;
import com.nexoshift.petcarehub.clinicalexams.domain.model.queries.GetClinicalExamsByPetIdAndStatusQuery;
import com.nexoshift.petcarehub.clinicalexams.domain.model.queries.GetClinicalExamsByPetIdQuery;
import com.nexoshift.petcarehub.clinicalexams.domain.model.queries.GetClinicalFileContentQuery;
import com.nexoshift.petcarehub.clinicalexams.domain.model.valueobjects.ClinicalFileContent;
import java.util.List;
import java.util.Optional;

public interface ClinicalExamQueryService {

    Optional<ClinicalExam> handle(GetClinicalExamByIdQuery query);

    List<ClinicalExam> handle(GetClinicalExamsByConsultationIdQuery query);

    List<ClinicalExam> handle(GetClinicalExamsByPetIdQuery query);

    List<ClinicalExam> handle(GetClinicalExamsByPetIdAndStatusQuery query);

    ClinicalFileContent handle(GetClinicalFileContentQuery query);
}
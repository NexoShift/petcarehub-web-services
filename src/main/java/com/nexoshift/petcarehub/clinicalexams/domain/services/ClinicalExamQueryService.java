package com.nexoshift.petcarehub.clinicalexams.domain.services;

import com.nexoshift.petcarehub.clinicalexams.domain.model.aggregates.ClinicalExam;
import com.nexoshift.petcarehub.clinicalexams.domain.model.queries.*;
import com.nexoshift.petcarehub.clinicalexams.domain.model.valueobjects.ClinicalFileContent;

import java.util.List;
import java.util.Optional;

public interface ClinicalExamQueryService {

    Optional<ClinicalExam> handle(GetClinicalExamByIdQuery query);

    List<ClinicalExam> handle(GetClinicalExamsByConsultationIdQuery query);

    List<ClinicalExam> handle(GetClinicalExamsByPetIdQuery query);

    List<ClinicalExam> handle(GetClinicalExamsByPetIdAndStatusQuery query);

    List<ClinicalExam> handle(GetClinicalExamsOfMyClinicQuery query);

    ClinicalFileContent handle(GetClinicalFileContentQuery query);
}
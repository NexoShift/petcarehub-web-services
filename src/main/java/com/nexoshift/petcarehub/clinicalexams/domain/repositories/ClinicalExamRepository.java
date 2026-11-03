package com.nexoshift.petcarehub.clinicalexams.domain.repositories;

import com.nexoshift.petcarehub.clinicalexams.domain.model.aggregates.ClinicalExam;
import com.nexoshift.petcarehub.clinicalexams.domain.model.valueobjects.ClinicalConsultationId;
import com.nexoshift.petcarehub.clinicalexams.domain.model.valueobjects.ExamStatus;
import com.nexoshift.petcarehub.clinicalexams.domain.model.valueobjects.PetId;
import java.util.List;
import java.util.Optional;

public interface ClinicalExamRepository {

    ClinicalExam save(ClinicalExam clinicalExam);

    Optional<ClinicalExam> findById(Long id);

    List<ClinicalExam> findAllByConsultationId(ClinicalConsultationId consultationId);

    List<ClinicalExam> findAllByPetId(PetId petId);

    List<ClinicalExam> findAllByPetIdAndStatus(PetId petId, ExamStatus status);
}
package com.nexoshift.petcarehub.clinicalexams.domain.model.queries;

import com.nexoshift.petcarehub.clinicalexams.domain.model.valueobjects.ExamStatus;

public record GetClinicalExamsByPetIdAndStatusQuery(Long petId, ExamStatus status) {
}
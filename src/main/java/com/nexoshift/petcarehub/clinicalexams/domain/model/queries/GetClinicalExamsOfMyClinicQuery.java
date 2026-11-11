package com.nexoshift.petcarehub.clinicalexams.domain.model.queries;

import com.nexoshift.petcarehub.clinicalexams.domain.model.valueobjects.ExamStatus;

/** Exams of the clinic of the authenticated veterinarian; status is optional (null = all). */
public record GetClinicalExamsOfMyClinicQuery(Long requesterUserId, ExamStatus status) {
}

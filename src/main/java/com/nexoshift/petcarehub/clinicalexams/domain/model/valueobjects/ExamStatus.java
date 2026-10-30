package com.nexoshift.petcarehub.clinicalexams.domain.model.valueobjects;

/** Status of a clinical exam: REQUESTED -> IN_PROCESS -> COMPLETED, or CANCELLED. */
public enum ExamStatus {
    REQUESTED,
    IN_PROCESS,
    COMPLETED,
    CANCELLED
}
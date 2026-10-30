package com.nexoshift.petcarehub.clinicalexams.domain.model.commands;

import com.nexoshift.petcarehub.clinicalexams.domain.model.valueobjects.ExamType;

public record RequestClinicalExamCommand(
        Long consultationId, Long requesterUserId, ExamType type, String indication) {

    public RequestClinicalExamCommand {
        if (consultationId == null || consultationId <= 0) {
            throw new IllegalArgumentException("Consultation id must be greater than zero");
        }
        if (type == null) {
            throw new IllegalArgumentException("Exam type is required");
        }
    }
}
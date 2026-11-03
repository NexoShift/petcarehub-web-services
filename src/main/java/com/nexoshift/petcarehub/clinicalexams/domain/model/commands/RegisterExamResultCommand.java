package com.nexoshift.petcarehub.clinicalexams.domain.model.commands;

import java.time.LocalDate;

public record RegisterExamResultCommand(
        Long examId, String summary, String values, String interpretation, LocalDate resultDate) {
}
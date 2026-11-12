package com.nexoshift.petcarehub.clinicalexams.domain.model.commands;

public record ValidateExamResultCommand(Long examId, Long validatorUserId) {
}
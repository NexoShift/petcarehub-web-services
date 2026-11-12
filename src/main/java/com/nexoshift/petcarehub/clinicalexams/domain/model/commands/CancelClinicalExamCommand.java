package com.nexoshift.petcarehub.clinicalexams.domain.model.commands;

public record CancelClinicalExamCommand(Long examId, Long requesterUserId, String reason) {
}

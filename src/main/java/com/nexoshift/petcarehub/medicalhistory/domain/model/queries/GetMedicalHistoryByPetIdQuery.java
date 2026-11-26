package com.nexoshift.petcarehub.medicalhistory.domain.model.queries;

public record GetMedicalHistoryByPetIdQuery(Long petId, Long requesterUserId) {
}

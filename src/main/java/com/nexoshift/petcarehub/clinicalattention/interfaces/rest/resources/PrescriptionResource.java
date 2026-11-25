package com.nexoshift.petcarehub.clinicalattention.interfaces.rest.resources;

import java.time.Instant;
import java.util.List;

public record PrescriptionResource(
    Long id, Instant issuedAt, String generalInstruction, List<MedicationResource> medications,
    Long prescribedByVeterinarianId) {
}

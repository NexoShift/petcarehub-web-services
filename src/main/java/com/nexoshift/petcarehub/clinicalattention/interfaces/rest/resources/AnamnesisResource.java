package com.nexoshift.petcarehub.clinicalattention.interfaces.rest.resources;

import java.util.List;

public record AnamnesisResource(
    Long id, String chiefComplaint, String evolutionTime, List<String> symptoms, String diet,
    String behavior, String previousTreatments, String ownerObservations) {
}

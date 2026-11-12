package com.nexoshift.petcarehub.clinicalattention.interfaces.rest.resources;

public record MedicationResource(Long id,
                                 String name,
                                 String dosage,
                                 String frequency,
                                 String duration,
                                 String indication) {
}

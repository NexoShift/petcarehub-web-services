package com.nexoshift.petcarehub.medicalhistory.interfaces.rest.resources;

import java.time.LocalDate;

public record VaccinationResource(Long id, String vaccineName, Integer doseNumber,
                                  LocalDate appliedOn, LocalDate nextDoseOn, String batchNumber,
                                  String notes, Long veterinarianId, Long clinicId) {
}

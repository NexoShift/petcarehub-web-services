package com.nexoshift.petcarehub.aiassistance.application.internal.outboundservices.reports;

import java.time.LocalDate;

/** What the confirmed report shows. */
public record ClinicalReportData(Long consultationId, String patientSummary,
    String currentReason, LocalDate date, String finalText, boolean edited, String model) {
}

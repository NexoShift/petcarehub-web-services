package com.nexoshift.petcarehub.aiassistance.application.internal.outboundservices.reports;

/** Port to the PDF generator. */
public interface ClinicalReportRenderer {

  byte[] render(ClinicalReportData data);
}
